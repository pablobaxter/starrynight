/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.frybits.starrynight.android.auth.impl

import android.security.keystore.KeyProperties
import android.util.Log
import com.frybits.starrynight.android.auth.LoggedInUserDataStore
import com.frybits.starrynight.android.auth.network.AuthApi
import com.frybits.starrynight.android.auth.utils.DpopKeyManager
import com.frybits.starrynight.android.auth.utils.DpopProofBuilder
import com.frybits.starrynight.atproto.ATProtoRepository
import com.frybits.starrynight.atproto.models.ATProtoSession
import com.frybits.starrynight.atproto.models.ATProtoSessionStatus
import com.frybits.starrynight.auth.AuthRepository
import com.frybits.starrynight.auth.LoggedInUserData
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val LOG_TAG = AuthRepository::class.java.simpleName

@ContributesBinding(AppScope::class)
@Inject
internal class AuthRepositoryImpl(
    private val atProtoRepository: ATProtoRepository,
    private val loggedInUserDataStore: LoggedInUserDataStore,
    private val encoder: Base64,
    private val keyManager: DpopKeyManager,
    private val proofBuilder: DpopProofBuilder,
    private val authApi: AuthApi
) : AuthRepository {
    private val secureRandom = SecureRandom.getInstanceStrong()

    private lateinit var currState: String

    private lateinit var verifier: String

    override suspend fun getCurrentUserFlow(): Flow<LoggedInUserData> {
        return loggedInUserDataStore.loggedInUserDataFlow
    }

    override suspend fun login(handle: String, password: String): Result<Unit> {
        return runCatching {
            val sessionData = atProtoRepository.createSession(handle, password).getOrElse {
                throw Exception("Error creating session", it)
            }

            loggedInUserDataStore.storeLoggedInUserData(
                LoggedInUserData(
                    did = sessionData.id,
                    handle = sessionData.handle,
                    email = sessionData.email,
                    active = sessionData.active,
                    status = sessionData.status?.name.orEmpty(),
                    token = sessionData.accessJwt,
                    refreshToken = sessionData.refreshJwt,
                    emailConfirmed = sessionData.emailConfirmed
                )
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun loginWithOAuth(handle: String): Result<Unit> {
        return runCatching {
            val did = atProtoRepository.resolveHandle(handle).getOrThrow()
            val resolvedDid = atProtoRepository.resolveDid(did).getOrThrow()
            val serverMetaData = atProtoRepository.getAuthServerMetaData(resolvedDid).getOrThrow()

            currState = Uuid.generateV7().toString()

            val tokenEndpoint = requireNotNull(serverMetaData["token_endpoint"]?.jsonPrimitive?.content) { "No token endpoint found" }
            verifier = encoder.encode(ByteArray(32).also { secureRandom.nextBytes(it) })
            val challenge = encoder.encode(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))

            val parEndpoint = requireNotNull(serverMetaData["pushed_authorization_request_endpoint"]?.jsonPrimitive?.content) { "No pushed authorization token endpoint found" }
            val authorizationEndpoint = requireNotNull(serverMetaData["authorization_endpoint"]?.jsonPrimitive?.content) { "No authorization endpoint found" }

            var nonce: String? = null
            repeat(2) { attempt ->
                val dpop = proofBuilder.create(
                    keyPair = keyManager.getOrCreate(),
                    method = "POST",
                    url = parEndpoint,
                    nonce = nonce
                )

                val request = FormBody.Builder()
                    .add("client_id", "https://starrynight.frybits.com/.well-known/client-metadata.json")
                    .add("response_type", "code")
                    .add("code_challenge", challenge)
                    .add("state", currState)
                    .add("redirect_uri", "https://starrynight.frybits.com/mobile/login")
                    .add("scope", "atproto transition:generic")
                    .add("application_type", "native")
                    .add("grant_types", "[authorization_code,refresh_token]")
                    .add("scope", "atproto")
                    .add("login_hint", handle)
                    .build()

                val response = authApi.doPar(parEndpoint, dpop, request)

                response.headers()["DPoP-Nonce"]?.let {
                    nonce = it
                    loggedInUserDataStore.storeLoggedInUserData(getCurrentUserFlow().first().copy(dpopNonce = it))
                }

                if (response.isSuccessful) {
                    Log.d("Blah", response.body().toString())
                }

                if (attempt == 0 && nonce != null) return@repeat // retry with nonce
                error("PAR failed after nonce retry")
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return runCatching {
            val currentUser = getCurrentUserFlow().first()
            val sessionData = ATProtoSession(
                id = currentUser.did,
                email = currentUser.email,
                active = currentUser.active,
                handle = currentUser.handle,
                status = runCatching { ATProtoSessionStatus.valueOf(currentUser.status) }
                    .onFailure { Log.d(LOG_TAG, "Unable to find status for ${currentUser.status}") }
                    .getOrNull(),
                accessJwt = currentUser.token,
                refreshJwt = currentUser.refreshToken,
                emailConfirmed = currentUser.emailConfirmed,
                emailAuthFactor = false
            )
            atProtoRepository.deleteSession(sessionData)
                .onFailure { Log.w(LOG_TAG, "Failure deleting session remotely", it) }

            loggedInUserDataStore.clearLoggedInUserData()
        }
    }

    override suspend fun refreshToken(force: Boolean): Result<LoggedInUserData> {
        return runCatching {
            val currentUser = getCurrentUserFlow().first()
            val sessionData = ATProtoSession(
                id = currentUser.did,
                email = currentUser.email,
                active = currentUser.active,
                handle = currentUser.handle,
                status = runCatching { ATProtoSessionStatus.valueOf(currentUser.status) }
                    .onFailure { Log.d(LOG_TAG, "Unable to find status for ${currentUser.status}") }
                    .getOrNull(),
                accessJwt = currentUser.token,
                refreshJwt = currentUser.refreshToken,
                emailConfirmed = currentUser.emailConfirmed,
                emailAuthFactor = false
            )
            val result = if (force) {
                atProtoRepository.refreshSession(sessionData)
            } else {
                atProtoRepository.getSession(sessionData)
            }

            return result.map {
                val loggedInUserData = LoggedInUserData(
                    did = sessionData.id,
                    handle = sessionData.handle,
                    email = sessionData.email,
                    active = sessionData.active,
                    status = sessionData.status?.name.orEmpty(),
                    token = sessionData.accessJwt,
                    refreshToken = sessionData.refreshJwt,
                    emailConfirmed = sessionData.emailConfirmed
                )
                loggedInUserDataStore.storeLoggedInUserData(loggedInUserData)
                return@map loggedInUserData
            }
        }
    }
}

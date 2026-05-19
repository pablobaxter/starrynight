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
import kotlinx.serialization.json.jsonPrimitive
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

private val LOG_TAG = AuthRepository::class.java.simpleName

@ContributesBinding(AppScope::class)
@Inject
internal class AuthRepositoryImpl(
    private val atProtoRepository: ATProtoRepository,
    private val loggedInUserDataStore: LoggedInUserDataStore
) : AuthRepository {

    private val base64Encoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
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

    override suspend fun loginWithOAuth(handle: String): Result<Unit> {
        return runCatching {
            val did = atProtoRepository.resolveHandle(handle).getOrThrow()
            val resolvedDid = atProtoRepository.resolveDid(did).getOrThrow()
            val serverMetaData = atProtoRepository.getAuthServerMetaData(resolvedDid).getOrThrow()

            currState = Uuid.random().toString()

            val tokenEndpoint = requireNotNull(serverMetaData["token_endpoint"]?.jsonPrimitive?.content) { "No token endpoint found" }
            verifier = base64Encoder.encode(ByteArray(32).also { secureRandom.nextBytes(it) })
            val challenge = base64Encoder.encode(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))

            val parEndpoint = requireNotNull(serverMetaData["pushed_authorization_request_endpoint"]?.jsonPrimitive?.content) { "No pushed authorization token endpoint found" }
            val authorizationEndpoint = requireNotNull(serverMetaData["authorization_endpoint"]?.jsonPrimitive?.content) { "No authorization endpoint found" }

            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")

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

/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 Pablo Baxter
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

package com.frybits.starrynight.android.network

import android.util.Log
import com.frybits.starrynight.auth.AuthRepository
import com.frybits.starrynight.auth.LoggedInUserDataStore
import com.frybits.starrynight.utils.core.dpop.DpopKeyManager
import com.frybits.starrynight.utils.core.dpop.DpopProofBuilder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private val LOG_TAG = AuthenticatorInterceptor::class.java.simpleName

@Inject
@SingleIn(AppScope::class)
internal class AuthenticatorInterceptor(
    private val dpopKeyManager: DpopKeyManager,
    private val dpopProofBuilder: DpopProofBuilder,
    private val loggedInUserDataStore: LoggedInUserDataStore,
    private val authRepositoryProvider: () -> AuthRepository
): Authenticator {

    private val mutex: Mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val paths = response.request.url.pathSegments
        // Skip refresh session paths
        if (paths.last() == "com.atproto.server.refreshSession") {
            return null
        }
        return runBlocking {
            return@runBlocking mutex.withLock {
                if (response.responseCount >= 5) {
                    Log.d(LOG_TAG, "Throttling response retries")
                    return@withLock null
                }
                val challenges = response.challenges()

                challenges.forEach { challenge ->
                    if (challenge.authParams["error"] == "use_dpop_nonce") {
                        val accessToken = response.request.header("Authorization")
                        val loggedInUserData = loggedInUserDataStore.loggedInUserDataFlow.first()
                        val nonce = response.header("DPoP-Nonce")?.also { loggedInUserDataStore.storeLoggedInUserData(loggedInUserData.copy(nonce = it)) }
                        val proof = try {
                            dpopProofBuilder.create(
                                keyPair = dpopKeyManager.getOrCreate(),
                                method = response.request.method,
                                url = response.request.url.toString(),
                                accessToken = accessToken?.split(' ')?.last(),
                                nonce = nonce
                            )
                        } catch (e: Exception) {
                            Log.e(LOG_TAG, "Failed to create DPoP proof", e)
                            throw e
                        }

                        return@withLock response.request.newBuilder()
                            .header("DPoP", proof)
                            .build()
                    } else if (challenge.authParams["error"] == "invalid_token") {
                        val authRepo = authRepositoryProvider()
                        val loggedInUserData = authRepo.refreshToken(true).getOrThrow()

                        return@withLock response.request.newBuilder()
                            .header("Authorization", "${loggedInUserData.tokenType ?: "Bearer"} ${loggedInUserData.token}")
                            .build()
                    }
                }
                return@withLock null
            }
        }
    }

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()
}

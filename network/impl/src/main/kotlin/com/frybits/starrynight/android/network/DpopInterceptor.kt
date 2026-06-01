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
import com.frybits.starrynight.auth.LoggedInUserDataStore
import com.frybits.starrynight.utils.core.dpop.DpopKeyManager
import com.frybits.starrynight.utils.core.dpop.DpopProofBuilder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

private val LOG_TAG = DpopInterceptor::class.java.simpleName

@Inject
@SingleIn(AppScope::class)
internal class DpopInterceptor(
    private val dpopKeyManager: DpopKeyManager,
    private val dpopProofBuilder: DpopProofBuilder,
    private val loggedInUserDataStore: LoggedInUserDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken = request.header("Authorization") ?: return chain.proceed(request)

        if (!accessToken.startsWith("DPoP")) {
            return chain.proceed(request)
        }

        Log.d(LOG_TAG, "DPoP interceptor hit")

        return runBlocking {
            val loggedInUserData = loggedInUserDataStore.loggedInUserDataFlow.first()
            val nonce = loggedInUserData.nonce

            val keyPair = dpopKeyManager.getOrCreate()

            val proof = try {
                dpopProofBuilder.create(
                    keyPair = keyPair,
                    method = request.method,
                    url = request.url.toString(),
                    accessToken = accessToken.split(' ').last(),
                    nonce = nonce
                )
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to create DPoP proof", e)
                throw e
            }

            val response = chain.proceed(
                request.newBuilder()
                    .header("DPoP", proof)
                    .build()
            )

            val newNonce = response.header("DPoP-Nonce")
            loggedInUserDataStore.storeLoggedInUserData(loggedInUserData.copy(nonce = newNonce))

            val challenges = response.challenges().map { it.parse() }

            // If there is a nonce challenge, retry with new nonce
            if (challenges.any { it == ChallengeType.NONCE }) {
                Log.d(LOG_TAG, "Hit nonce challenge")
                val newProof = try {
                    dpopProofBuilder.create(
                        keyPair = keyPair,
                        method = request.method,
                        url = request.url.toString(),
                        accessToken = accessToken.split(' ').last(),
                        nonce = newNonce
                    )
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Failed to create DPoP proof", e)
                    throw e
                }

                val newResponse = chain.proceed(
                    request.newBuilder()
                        .header("DPoP", newProof)
                        .build()
                )

                // Store the absolute latest nonce
                newResponse.header("DPoP-Nonce")?.let { loggedInUserDataStore.storeLoggedInUserData(loggedInUserData.copy(nonce = it)) }
                return@runBlocking newResponse
            }

            return@runBlocking response
        }
    }
}

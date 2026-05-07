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

package com.frybits.starrynight.android.network.auth.impl

import com.frybits.starrynight.network.core.ATProtoRepository
import com.frybits.starrynight.network.core.LoginRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import retrofit2.Retrofit

private val TAG = LoginRepository::class.java.simpleName

@ContributesBinding(AppScope::class)
@Inject
internal class LoginRepositoryImpl(
    private val retrofit: Retrofit,
    private val atprotoRepository: ATProtoRepository
) : LoginRepository {
    override suspend fun createSession(
        identifier: String,
        password: String
    ): Result<Unit> {
        atprotoRepository.resolveHandle(identifier)
//        val handleResponse = resolveHandleApi.resolveHandle(identifier)
//        Log.d(TAG, "Respons: ${handleResponse.code()}")
//        Log.d(TAG, "Message: ${handleResponse.errorBody().use { it?.string() }}")
//        val did = handleResponse.body()?.did ?: return Result.failure(Exception())
//
//        Log.d(TAG, "Got did: $did")

//        val didResponse = resolveDidApi.resolveDid("did:plc:cguwfiesizkt4ssgx6q7d6yy")
//        Log.d(TAG, "Code: ${didResponse.code()}")
//        val service = didResponse.body()
//        Log.d(TAG, "Got did doc: ${service?.didDoc}")

        return Result.success(Unit)

//        val result = createSessionApi.createSession(
//            requestBody = CreateSessionRequest(
//                password = password,
//                identifier = identifier
//            )
//        )
//
//        if (result.isSuccessful) {
//            Log.d(TAG, "Success")
//            Log.d(TAG, result.body().toString())
//            return Result.success(Unit)
//        } else {
//            Log.d(TAG, "Fail")
//            Log.d(TAG, "HTTP Code: ${result.code()}")
//            Log.d(TAG, "HTTP Error: ${result.message()}")
//            Log.d(TAG, result.errorBody().use { it?.string() }.toString())
//            return Result.failure(Exception(result.message()))
//        }
    }
}

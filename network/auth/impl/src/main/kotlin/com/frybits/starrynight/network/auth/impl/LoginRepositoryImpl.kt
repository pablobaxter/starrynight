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

package com.frybits.starrynight.network.auth.impl

import android.util.Log
import com.atproto.server.createSession.CreateSessionApi
import com.atproto.server.createSession.CreateSessionRequest
import com.frybits.starrynight.network.core.LoginRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

private val TAG = LoginRepository::class.java.simpleName

@ContributesBinding(AppScope::class)
@Inject
internal class LoginRepositoryImpl(
    private val createSessionApi: CreateSessionApi
) : LoginRepository {
    override suspend fun createSession(
        identifier: String,
        password: String
    ): Result<Unit> {
        val result = createSessionApi.createSession(
            requestBody = CreateSessionRequest(
                password = password,
                identifier = identifier
            )
        )

        if (result.isSuccessful) {
            Log.d(TAG, "Success")
            Log.d(TAG, result.body().toString())
            return Result.success(Unit)
        } else {
            Log.d(TAG, "Fail")
            Log.d(TAG, "HTTP Code: ${result.code()}")
            Log.d(TAG, "HTTP Error: ${result.message()}")
            Log.d(TAG, result.errorBody().use { it?.string() }.toString())
            return Result.failure(Exception(result.message()))
        }
    }
}

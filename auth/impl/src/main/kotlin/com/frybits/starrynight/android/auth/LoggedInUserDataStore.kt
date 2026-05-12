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

package com.frybits.starrynight.android.auth

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.frybits.starrynight.auth.LoggedInUserData
import com.google.protobuf.InvalidProtocolBufferException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

internal interface LoggedInUserDataStore {

    suspend fun storeLoggedInUserData(userData: LoggedInUserData)

    val loggedInUserDataFlow: Flow<LoggedInUserData>
}

@ContributesBinding(AppScope::class)
@Inject
internal class LoggedInUserDataStoreImpl(
    private val context: Context
) : LoggedInUserDataStore {

    private val Context.datastore: DataStore<LoggedInUserProto> by dataStore(
        fileName = "loggedInUser.pb",
        serializer = LoggedInUserSerializer
    )

    override suspend fun storeLoggedInUserData(userData: LoggedInUserData) {
        context.datastore.updateData { loggedInUser ->
            loggedInUser.copy {
                did = userData.did
                handle = userData.handle
                email = userData.email
                active = userData.active
                status = userData.status
                token = userData.token
                refreshToken = userData.refreshToken
                emailConfirmed = userData.emailConfirmed
            }
        }
    }

    override val loggedInUserDataFlow: Flow<LoggedInUserData> = context.datastore.data.map {
        LoggedInUserData(
            did = it.did,
            handle = it.handle,
            email = it.email,
            active = it.active,
            status = it.status,
            token = it.token,
            refreshToken = it.refreshToken,
            emailConfirmed = it.emailConfirmed
        )
    }
}

private object LoggedInUserSerializer : Serializer<LoggedInUserProto> {
    override val defaultValue: LoggedInUserProto = LoggedInUserProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LoggedInUserProto {
        try {
            return LoggedInUserProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: LoggedInUserProto,
        output: OutputStream
    ) {
        return t.writeTo(output)
    }
}

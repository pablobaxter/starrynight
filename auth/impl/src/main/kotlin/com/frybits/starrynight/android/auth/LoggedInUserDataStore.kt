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
import androidx.datastore.tink.AeadSerializer
import com.frybits.starrynight.auth.LoggedInUserData
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal interface LoggedInUserDataStore {

    suspend fun storeLoggedInUserData(userData: LoggedInUserData)

    suspend fun clearLoggedInUserData()

    val loggedInUserDataFlow: Flow<LoggedInUserData>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class LoggedInUserDataStoreImpl(
    private val context: Context
) : LoggedInUserDataStore {
    private val keysetHandle = AndroidKeysetManager.Builder()
        .withSharedPref(context, "keyset", "keyset_prefs")
        .withKeyTemplate(KeyTemplate.createFrom(PredefinedAeadParameters.AES256_GCM))
        .withMasterKeyUri("android-keystore://master_key")
        .build()
        .keysetHandle

    private val aeadSerializer = AeadSerializer(
        aead = keysetHandle.getPrimitive(
            RegistryConfiguration.get(),
            Aead::class.java
        ),
        wrappedSerializer = LoggedInUserSerializer,
        associatedData = "starry-night.loggedInUser.pb".encodeToByteArray()
    )

    private val Context.datastore: DataStore<LoggedInUserProto> by dataStore(
        fileName = "loggedInUser.pb",
        serializer = aeadSerializer
    )

    override suspend fun storeLoggedInUserData(userData: LoggedInUserData) {
        context.datastore.updateData { loggedInUser ->
            return@updateData loggedInUser.copy(
                did = userData.did,
                handle = userData.handle,
                email = userData.email,
                active = userData.active,
                status = userData.status,
                token = userData.token,
                refreshToken = userData.refreshToken,
                emailConfirmed = userData.emailConfirmed
            )
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

    override suspend fun clearLoggedInUserData() {
        context.datastore.updateData {
            return@updateData LoggedInUserProto()
        }
    }
}

private object LoggedInUserSerializer : Serializer<LoggedInUserProto> {
    override val defaultValue: LoggedInUserProto = LoggedInUserProto()

    override suspend fun readFrom(input: InputStream): LoggedInUserProto {
        try {
            return LoggedInUserProto.ADAPTER.decode(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: LoggedInUserProto,
        output: OutputStream
    ) {
        return LoggedInUserProto.ADAPTER.encode(output, t)
    }
}

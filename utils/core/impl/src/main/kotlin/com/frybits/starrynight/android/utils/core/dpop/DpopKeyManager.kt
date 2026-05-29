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

package com.frybits.starrynight.android.utils.core.dpop

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.frybits.starrynight.utils.core.DefaultDispatcher
import com.frybits.starrynight.utils.core.dpop.DpopKeyManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec

private const val ALIAS = "atproto_dpop_key"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DpopKeyManagerImpl(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : DpopKeyManager {

    override suspend fun getOrCreate(): KeyPair = withContext(defaultDispatcher) {
        val keystore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

        if (!keystore.containsAlias(ALIAS)) {
            val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEY_STORE)
            keyPairGenerator.initialize(
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                ).setAlgorithmParameterSpec(
                    ECGenParameterSpec("secp256r1")
                ).setDigests(KeyProperties.DIGEST_SHA256)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
            keyPairGenerator.generateKeyPair()
        }

        val entry = keystore.getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
        return@withContext KeyPair(entry.certificate.publicKey, entry.privateKey)
    }
}

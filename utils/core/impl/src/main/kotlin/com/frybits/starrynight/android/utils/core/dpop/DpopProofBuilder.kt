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

import com.frybits.starrynight.utils.core.DefaultDispatcher
import com.frybits.starrynight.utils.core.dpop.DpopProofBuilder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.security.KeyPair
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.ECPublicKey
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DpopProofBuilderImpl(
    private val encoder: Base64,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
): DpopProofBuilder {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun create(
        keyPair: KeyPair,
        method: String,
        url: String,
        accessToken: String?,
        nonce: String?
    ): String = withContext(defaultDispatcher) {
        val publicKey = keyPair.public as ECPublicKey
        val privateKey = keyPair.private

        val header = buildJsonObject {
            put("typ", "dpop+jwt")
            put("alg", "ES256")
            putJsonObject("jwk") {
                publicKey.toJwkMap(encoder).forEach { (key, value) ->
                    put(key, value)
                }
            }
        }

        val payload = buildJsonObject {
            put("jti", Uuid.generateV7().toString())
            put("iat", Clock.System.now().epochSeconds)
            put("htm", method.uppercase())
            put("htu", url.substringBefore('?'))

            nonce?.let { put("nonce", it) }
            accessToken?.let {
                val hash = MessageDigest.getInstance("SHA-256").digest(it.toByteArray())
                put("ath", encoder.encode(hash))
            }
        }

        val signingInput = "${encoder.encode(header.toString().toByteArray())}.${encoder.encode(payload.toString().toByteArray())}"

        val signature = Signature.getInstance("SHA256withECDSA").run {
            initSign(privateKey)
            update(signingInput.toByteArray())
            sign()
        }

        val signed = encoder.encode(signature.derToP1363())

        return@withContext "${signingInput}.${signed}"
    }

    private fun ByteArray.derToP1363(): ByteArray {
        var offset = 2

        require(this[offset++].toInt() == 0x02)
        val rLen = this[offset++].toInt()
        val r = copyOfRange(offset, offset + rLen)
        offset += rLen

        require(this[offset++].toInt() == 0x02)
        val sLen = this[offset++].toInt()
        val s = copyOfRange(offset, offset + sLen)

        return r.toFixed32() + s.toFixed32()
    }
}

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

package com.frybits.starrynight.android.auth.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.security.KeyPair
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal interface DpopProofBuilder {

    fun create(
        keyPair: KeyPair,
        method: String,
        url: String,
        accessToken: String? = null,
        nonce: String? = null
    ): String
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DpopProofBuilderImpl(
    private val encoder: Base64
): DpopProofBuilder {

    @OptIn(ExperimentalUuidApi::class)
    override fun create(
        keyPair: KeyPair,
        method: String,
        url: String,
        accessToken: String?,
        nonce: String?
    ): String {
        val publicKey = keyPair.public as ECPublicKey
        val privateKey = keyPair.private as ECPrivateKey

        val jwt = JWT.create()
            .withHeader(
                mapOf(
                    "typ" to "dpop+jwt",
                    "alg" to "ES256",
                    "jwk" to publicKey.toJwkMap(encoder)
                )
            )
            .withJWTId(Uuid.generateV7().toString())
            .withIssuedAt(Clock.System.now().toJavaInstant())
            .withClaim("htm", method.uppercase(Locale.US))
            .withClaim("htu", url.substringBefore('?'))

        nonce?.let { jwt.withClaim("nonce", it) }
        accessToken?.let {
            val hash = MessageDigest.getInstance("SHA-256").digest(it.toByteArray())
            jwt.withClaim("ath", encoder.encode(hash))
        }

        Signature.getInstance("SHA256withECDSA").apply {
            initSign(keyPair.private)
        }

        return jwt.sign(Algorithm.ECDSA256(publicKey, privateKey))
    }
}

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

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SignatureException
import kotlin.io.encoding.Base64

internal class AndroidJwtAlgorithm(
    private val publicKey: PublicKey,
    private val privateKey: PrivateKey
): Algorithm("ES256", "SHA256withECDSA") {

    override fun sign(contentBytes: ByteArray): ByteArray {

    }

    override fun verify(jwt: DecodedJWT) {
        try {
            val signatureBytes = Base64.UrlSafe.decode(jwt.signature)
            require(signatureBytes.size == 32 * 2) { throw SignatureException("Invalid JOSE signature format.") }
            require(!signatureBytes.isAllZeros()) { throw SignatureException("Invalid signature format.") }

            val rBytes = signatureBytes.copyOfRange(0, 32)
            require(!rBytes.isAllZeros()) { throw SignatureException("Invalid signature format.") }

            val sBytes= signatureBytes.copyOfRange(32, 64)
            require(!sBytes.isAllZeros()) { throw SignatureException("Invalid signature format.") }


        }
    }
}

private fun ByteArray.isAllZeros(): Boolean {
    return all { it == 0.toByte() }
}

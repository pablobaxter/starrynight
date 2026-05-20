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

import java.math.BigInteger
import java.security.interfaces.ECPublicKey
import kotlin.io.encoding.Base64

internal fun ECPublicKey.toJwkMap(encoder: Base64): Map<String, String> {
    fun BigInteger.toP256Bytes(): ByteArray {
        val raw = toByteArray()
        return when {
            raw.size == 33 && raw[0] == 0.toByte() -> raw.copyOfRange(1, 33)
            raw.size < 32 -> ByteArray(32 - raw.size) + raw
            else -> raw
        }
    }

    return mapOf(
        "kty" to "EC",
        "crv" to "P-256",
        "x" to encoder.encode(w.affineX.toP256Bytes()),
        "y" to encoder.encode(w.affineY.toP256Bytes())
    )
}

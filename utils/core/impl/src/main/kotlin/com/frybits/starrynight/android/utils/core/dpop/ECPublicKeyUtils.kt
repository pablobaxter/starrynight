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

import java.math.BigInteger
import java.security.interfaces.ECPublicKey
import kotlin.io.encoding.Base64

internal fun ECPublicKey.toJwkMap(encoder: Base64): Map<String, String> {
    return mapOf(
        "crv" to "P-256",
        "kty" to "EC",
        "x" to encoder.encode(w.affineX.toFixed32()),
        "y" to encoder.encode(w.affineY.toFixed32())
    )
}

internal fun BigInteger.toFixed32(): ByteArray {
    return toByteArray().toFixed32()
}

internal fun ByteArray.toFixed32(): ByteArray {
    return when {
        size == 33 && this[0] == 0.toByte() -> copyOfRange(1, 33)
        size < 32 -> ByteArray(32 - size) + this
        else -> this
    }
}

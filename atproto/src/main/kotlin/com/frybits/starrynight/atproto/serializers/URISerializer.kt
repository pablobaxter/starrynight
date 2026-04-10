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

package com.frybits.starrynight.atproto.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URI

internal object URISerializer: KSerializer<URI> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.net.URI", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): URI {
        return URI.create(decoder.toString())
    }

    override fun serialize(encoder: Encoder, value: URI) {
        encoder.encodeString(value.toString())
    }
}

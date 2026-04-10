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
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64

internal object ATBytesSerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ByteArray", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ByteArray {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        val result = element.jsonObject[$$"$bytes"]?.jsonPrimitive?.content.orEmpty()
        return Base64.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(result)
    }

    override fun serialize(encoder: Encoder, value: ByteArray) {
        val jsonEncoder = encoder as JsonEncoder
        val primitive = jsonEncoder.json.encodeToJsonElement(Base64.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).encode(value))
        jsonEncoder.encodeJsonElement(JsonObject(mapOf($$"$bytes" to primitive)))
    }
}

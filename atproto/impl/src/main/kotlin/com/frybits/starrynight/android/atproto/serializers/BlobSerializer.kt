/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 pablo
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

package com.frybits.starrynight.android.atproto.serializers

import com.frybits.starrynight.android.atproto.models.blob.Blob
import com.frybits.starrynight.android.atproto.models.blob.LegacyBlob
import com.frybits.starrynight.android.atproto.models.blob.StandardBlob
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.collections.contains

internal object BlobSerializer : JsonContentPolymorphicSerializer<Blob>(Blob::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Blob> {
        return if (element.jsonObject.contains("ref")) {
            StandardBlob.serializer()
        } else {
            LegacyBlob.serializer()
        }
    }
}

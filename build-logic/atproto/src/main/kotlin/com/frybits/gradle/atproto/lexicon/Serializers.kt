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

package com.frybits.gradle.atproto.lexicon

import com.frybits.gradle.atproto.lexicon.categories.AccountPermissionField
import com.frybits.gradle.atproto.lexicon.categories.BlobPermissionField
import com.frybits.gradle.atproto.lexicon.categories.IdentityPermissionField
import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.frybits.gradle.atproto.lexicon.categories.PermissionField
import com.frybits.gradle.atproto.lexicon.categories.RepoPermissionField
import com.frybits.gradle.atproto.lexicon.categories.RpcPermissionField
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.collections.contains

internal val lexiconJson = Json { serializersModule = lexiconSerializerModule() }

internal object LimitedScopeDeserializer : JsonTransformingSerializer<Map<String, LexiconType>>(serializer()) {
    val validTypes = setOf("boolean", "integer", "string")
    override fun transformDeserialize(element: JsonElement): JsonElement {
        require(element is JsonObject) { "JsonObject expected, got ${element::class}" }
        val check = element.values.all { data ->
            when (data) {
                is JsonObject -> {
                    val result = data["type"]?.jsonPrimitive?.content
                    if (result == "array") {
                        data["items"]?.jsonObject["type"]?.jsonPrimitive?.content in validTypes
                    } else {
                        data["type"]?.jsonPrimitive?.content in validTypes
                    }
                }

                else -> false
            }
        }
        require(check) { "Expected properties to contain type of $validTypes in elements ${element.values}" }
        return element
    }
}

private fun lexiconSerializerModule(): SerializersModule = SerializersModule {
    polymorphic(LexiconType::class) {
        defaultDeserializer { type ->
            if (type == "permission") {
                object : JsonContentPolymorphicSerializer<PermissionField>(PermissionField::class) {
                    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<PermissionField> {
                        val resource = element.jsonObject["resource"]?.jsonPrimitive?.content
                        return when (resource) {
                            "repo" -> RepoPermissionField.serializer()
                            "rpc" -> RpcPermissionField.serializer()
                            "blob" -> BlobPermissionField.serializer()
                            "identity" -> IdentityPermissionField.serializer()
                            "account" -> AccountPermissionField.serializer()
                            else -> error("Wrong stuff")
                        }
                    }
                }
            } else {
                error("Unknown type: $type")
            }
        }
    }
}

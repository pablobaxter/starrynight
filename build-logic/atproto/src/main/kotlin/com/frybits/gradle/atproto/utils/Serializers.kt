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

package com.frybits.gradle.atproto.utils

import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.frybits.gradle.atproto.lexicon.categories.PermissionField
import com.frybits.gradle.atproto.lexicon.categories.RepoPermissionField
import com.frybits.gradle.atproto.lexicon.categories.RpcPermissionField
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.collections.contains

internal val lexiconJson = Json {
    serializersModule = lexiconSerializerModule()
}

internal object ParamsLimitedProperties : JsonTransformingSerializer<Map<String, LexiconType>>(serializer()) {
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

internal object RecordLimitedProperties : JsonTransformingSerializer<LexiconType>(serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val data = element.jsonObject
        val result = data["type"]?.jsonPrimitive?.content
        require(result == "object") { "Record expected 'object' type, got $result" }
        return element
    }
}

internal object XRPCParamsLimitedProperties : JsonTransformingSerializer<LexiconType>(serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val data = element.jsonObject
        val result = data["type"]?.jsonPrimitive?.content
        require(result == "params") { "XRPC expected 'params' type, got $result" }
        return element
    }
}

internal object HttpBodyLimitedProperties : JsonTransformingSerializer<LexiconType>(serializer()) {
    val validTypes = setOf("object", "ref", "union", null)
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val data = element.jsonObject
        val result = data["schema"]?.jsonObject?.get("type")?.jsonPrimitive?.content
        require(result in validTypes) { "Response expected schema type $validTypes, got $result" }
        return element
    }
}

internal object SubscriptionLimitedProperties : JsonTransformingSerializer<LexiconType>(serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val result = element.jsonObject["type"]?.jsonPrimitive?.content
        require(result == "union") { "Response expected schema type 'union', got $result" }
        return element
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun lexiconSerializerModule(): SerializersModule = SerializersModule {
    polymorphic(LexiconType::class) {
        defaultDeserializer { type ->
            if (type == "permission") {
                object : JsonContentPolymorphicSerializer<PermissionField>(PermissionField::class) {
                    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<PermissionField> {
                        val resource = element.jsonObject["resource"]?.jsonPrimitive?.content
                        return when (resource) {
                            "repo" -> {
                                val collection = element.jsonObject["collection"]?.jsonArray
                                requireNotNull(collection) { "Property 'collection' must not be null" }
                                val result = collection.map { it.jsonPrimitive.content }
                                require(!result.contains("*")) { "Property 'collection' must not contain '*' wildcard" }
                                require(result.size == result.distinct().size) { "Property 'collection' must have unique items" }
                                require(result.isNotEmpty()) { "Property 'collection' must not be empty" }
                                RepoPermissionField.serializer()
                            }
                            "rpc" -> {
                                val lxm = element.jsonObject["lxm"]?.jsonArray
                                requireNotNull(lxm) { "Property 'lxm' must not be null" }
                                val result = lxm.map { it.jsonPrimitive.content }
                                require(result.isNotEmpty()) { "Property 'lxm' must not be empty" }
                                require(!result.contains("*")) { "Property 'lxm' must not contain '*' wildcard" }

                                val inheritAud = element.jsonObject["inheritAud"]?.jsonPrimitive?.content.toBoolean()

                                val aud = element.jsonObject["aud"]?.jsonPrimitive?.content
                                if (inheritAud) {
                                    require(aud == "*" || aud == null) { "Property 'aud' must be '*' wildcard or null with inheritAud" }
                                } else {
                                    requireNotNull(aud) { "Property 'aud' must not be null" }
                                }
                                RpcPermissionField.serializer()
                            }
                            else -> NothingSerializer()
                        }
                    }
                }
            } else {
                error("Unknown type: $type")
            }
        }
    }
}

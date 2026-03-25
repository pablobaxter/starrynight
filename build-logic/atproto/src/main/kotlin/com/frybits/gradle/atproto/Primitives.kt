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

package com.frybits.gradle.atproto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("boolean")
internal data class BooleanField(
    override val description: String? = null,
    val default: Boolean? = null,
    val const: Boolean? = null
): Field

@Serializable
@SerialName("integer")
internal data class IntegerField(
    override val description: String? = null,
    val minimum: Int? = null,
    val maximum: Int? = null,
    val enum: List<Int>? = null,
    val default: Int? = null,
    val const: Int? = null
): Field

@Serializable
@SerialName("string")
internal data class StringField(
    override val description: String? = null,
    val format: String? = null,
    val maxLength: Int? = null,
    val minLength: Int? = null,
    val maxGraphemes: Int? = null,
    val minGraphemes: Int? = null,
    val knownValues: List<String>? = null,
    val enum: List<String>? = null,
    val default: String? = null,
    val const: String? = null
): Field

@Serializable
@SerialName("bytes")
internal data class BytesField(
    override val description: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null
): Field

@Serializable
@SerialName("cid-link")
internal data class CidLinkField(
    override val description: String? = ""
): Field

@Serializable
@SerialName("array")
internal data class ArrayField(
    override val description: String? = null,
    val items: Field,
    val minLength: Int? = null,
    val maxLength: Int? = null
): Field

@Serializable
@SerialName("object")
internal data class ObjectField(
    override val description: String? = null,
    val properties: Map<String, Field>,
    val required: List<String>? = null,
    val nullable: List<String>? = null
): Field

@Serializable
@SerialName("blob")
internal data class BlobField(
    override val description: String? = null,
    val accept: List<String>? = null,
    val maxSize: Int? = null
): Field

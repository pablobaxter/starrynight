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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

internal sealed interface SubTypeField: LexiconType

@Serializable
@SerialName("params")
internal data class ParamsField(
    override val description: String? = null,
    val required: List<String>? = null,
    @Serializable(LimitedScopeDeserializer::class) val properties: Map<String, LexiconType>
): SubTypeField

@OptIn(ExperimentalSerializationApi::class)
@SerialName("permission")
internal sealed interface PermissionField: SubTypeField {
    val resource: String
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
internal data class RepoPermissionField(
    override val description: String? = null,
    override val resource: String,
    val collection: List<String>,
    val action: Set<String>? = null
): PermissionField

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
internal data class RpcPermissionField(
    override val description: String? = null,
    override val resource: String,
    val lxm: List<String>,
    val aud: String = "",
    val inheritAud: Boolean? = null
): PermissionField

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
internal data class BlobPermissionField(
    override val description: String? = null,
    override val resource: String,
    val accept: List<String>
): PermissionField

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
internal data class IdentityPermissionField(
    override val description: String? = null,
    override val resource: String,
    val attr: String
): PermissionField

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
internal data class AccountPermissionField(
    override val description: String? = null,
    override val resource: String,
    val attr: String,
    val action: String? = null
): PermissionField

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

package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.utils.ParamsLimitedProperties
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
internal sealed interface SubTypeField: LexiconType

@Serializable
@SerialName("params")
internal data class ParamsField(
    override val description: String? = null,
    val required: List<String>? = null,
    @Serializable(ParamsLimitedProperties::class) val properties: Map<String, LexiconType>
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
    val collection: Set<String>,
    val action: Set<Action>? = null
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

internal enum class Action {
    @SerialName("create")
    CREATE,

    @SerialName("update")
    UPDATE,

    @SerialName("delete")
    DELETE
}

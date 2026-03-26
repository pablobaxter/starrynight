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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal sealed interface MetaField: LexiconType

@Serializable
@SerialName("token")
internal data class TokenField(
    override val description: String? = null
): MetaField

@Serializable
@SerialName("ref")
internal data class RefField(
    override val description: String? = null,
    val ref: String
): MetaField

@Serializable
@SerialName("union")
internal data class UnionField(
    override val description: String? = null,
    val refs: List<String>,
    val closed: Boolean = false
): MetaField

@Serializable
@SerialName("unknown")
internal data class UnknownField(
    override val description: String? = null,
): MetaField

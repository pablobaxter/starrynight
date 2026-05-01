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

import com.frybits.gradle.atproto.utils.HttpBodyLimitedProperties
import com.frybits.gradle.atproto.utils.RecordLimitedProperties
import com.frybits.gradle.atproto.utils.SubscriptionLimitedProperties
import com.frybits.gradle.atproto.utils.XRPCParamsLimitedProperties
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface PrimaryField: LexiconType

@Serializable
@SerialName("record")
internal data class RecordField(
    override val description: String? = null,
    val key: String,
    @Serializable(RecordLimitedProperties::class) val record: LexiconType
): PrimaryField

@Serializable
internal sealed interface XRPCField: PrimaryField {
    val parameters: LexiconType?
    val errors: List<ErrorBodyField>?
}

@Serializable
internal sealed interface HttpField: XRPCField {
    val output: BodyField?
}

@Serializable
@SerialName("query")
internal data class QueryField(
    override val description: String? = null,
    @Serializable(XRPCParamsLimitedProperties::class) override val parameters: LexiconType? = null,
    override val output: BodyField? = null,
    override val errors: List<ErrorBodyField>? = null
): HttpField

@Serializable
@SerialName("procedure")
internal data class ProcedureField(
    override val description: String? = null,
    @Serializable(XRPCParamsLimitedProperties::class) override val parameters: LexiconType? = null,
    override val output: BodyField? = null,
    override val errors: List<ErrorBodyField>? = null,
    val input: BodyField? = null
): HttpField

@Serializable
@SerialName("subscription")
internal data class SubscriptionField(
    override val description: String? = null,
    @Serializable(XRPCParamsLimitedProperties::class) override val parameters: LexiconType? = null,
    val message: MessageField,
    override val errors: List<ErrorBodyField>? = null
): XRPCField

@Serializable
@SerialName("permission-set")
internal data class PermissionSetField(
    override val description: String? = null,
    val title: String? = null,
    @SerialName("title:lang") val titleLang: Map<String, String>? = null,
    val detail: String? = null,
    @SerialName("detail:lang") val detailLang: Map<String, String>? = null,
    val permissions: List<LexiconType>
): PrimaryField

@Serializable
internal data class BodyField(
    override val description: String? = null,
    val encoding: String,
    @Serializable(HttpBodyLimitedProperties::class) val schema: LexiconType? = null,
): LexiconType

@Serializable
internal data class ErrorBodyField(
    override val description: String? = null,
    val name: String
): LexiconType

@Serializable
internal data class MessageField(
    override val description: String? = null,
    @Serializable(SubscriptionLimitedProperties::class) val schema: LexiconType? = null,
): LexiconType

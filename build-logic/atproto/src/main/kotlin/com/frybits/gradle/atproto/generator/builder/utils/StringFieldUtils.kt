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

package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.StringFormat
import com.frybits.gradle.atproto.utils.camelToSnakeCase
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.annotated
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.Serializable
import java.net.URI
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun TypeSpec.Builder.handleConst(name: String, lexiconType: StringField) {
    val const = lexiconType.const ?: return
    val typeName = lexiconType.getTypeName()
    val property = PropertySpec.builder(name.camelToSnakeCase().uppercase(), typeName)
        .addModifiers(KModifier.PUBLIC)
        .initializer("%L", const)

    when (lexiconType.format) {
        StringFormat.DATETIME -> property.initializer("%T.parse(%S)", Instant::class, const)
        StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> property.initializer("%T.create(%S)", URI::class, const)
        null -> property.initializer("%S", const).addModifiers(KModifier.CONST)
        StringFormat.AT_IDENTIFIER -> {
            if (const.startsWith("did:")) {
                property.initializer("%T(%S)", TypeNames.Did, const)
            } else {
                property.initializer("%T(%S)", TypeNames.Handle, const)
            }
        }
        else -> property.initializer("%T(%S)", typeName, const)
    }

    property.handleDescription(lexiconType)

    addProperty(property.build())
}

internal fun FunSpec.Builder.handleParam(name: String, lexiconType: StringField, isRequired: Boolean, isNullable: Boolean) {
    val typeName = lexiconType.getTypeName().copy(nullable = isNullable)
    val parameter = ParameterSpec.builder(name, typeName)
    parameter.handleDescription(lexiconType)

    val default = lexiconType.default
    if (default != null) {
        parameter.defaultValue("%L", default)
    } else if (!isRequired) {
        if (isNullable) {
            parameter.defaultValue("%L", null)
        } else {
            parameter.defaultValue("%L", 0)
        }
    }

    addParameter(parameter.build())
}


@OptIn(ExperimentalTime::class)
private fun StringField.getTypeName(): TypeName {
    return when (format) {
        StringFormat.AT_IDENTIFIER -> TypeNames.ATIdentifier
        StringFormat.DID -> TypeNames.Did
        StringFormat.HANDLE -> TypeNames.Handle
        StringFormat.NSID -> TypeNames.Nsid
        StringFormat.TID -> TypeNames.Tid
        StringFormat.RECORD_KEY -> TypeNames.RecordKey
        StringFormat.LANGUAGE -> TypeNames.Language
        StringFormat.DATETIME -> Instant::class.asTypeName()
            .annotated(
                AnnotationSpec.builder(Serializable::class)
                    .addMember("%T::class", TypeNames.DateTimeSerializer)
                    .build()
            )
        StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> URI::class.asTypeName()
            .annotated(
                AnnotationSpec.builder(Serializable::class)
                    .addMember("%T::class", TypeNames.URISerializer)
                    .build()
            )
        null -> String::class.asTypeName()
    }
}

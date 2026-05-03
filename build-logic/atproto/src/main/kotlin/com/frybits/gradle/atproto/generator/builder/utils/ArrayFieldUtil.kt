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

import com.frybits.gradle.atproto.generator.builder.generateClass
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.categories.ArrayField
import com.frybits.gradle.atproto.lexicon.categories.BlobField
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.frybits.gradle.atproto.lexicon.categories.CidLinkField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField
import com.frybits.gradle.atproto.utils.LexiconRef
import com.frybits.gradle.atproto.utils.titleCaseFirstChar
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.json.JsonObject
import org.gradle.api.GradleException
import java.net.URI

internal fun FunSpec.Builder.handleParam(name: String, lexiconType: ArrayField, isRequired: Boolean, isNullable: Boolean, environment: LexiconEnvironment, context: LexiconContext) {
    val listTypeName = List::class.asTypeName()
    val parameterizedType = lexiconType.items.toTypeName(environment, context)

    val typeName = listTypeName.parameterizedBy(parameterizedType).copy(nullable = isNullable)

    val parameter = ParameterSpec.builder(name, typeName)
    parameter.handleDescription(lexiconType)

    if (!isRequired) {
        if (isNullable) {
            parameter.defaultValue("%L", null)
        } else {
            parameter.defaultValue("emptyList()")
        }
    }

    addParameter(parameter.build())
}

internal fun TypeSpec.Builder.handleProperty(name: String, lexiconType: ArrayField, isNullable: Boolean, environment: LexiconEnvironment, context: LexiconContext) {
    val listTypeName = List::class.asTypeName()
    val parameterizedType = lexiconType.items.toTypeName(environment, context)

    val typeName = listTypeName.parameterizedBy(parameterizedType).copy(nullable = isNullable)

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    property.handleDescription(lexiconType)

    addProperty(property.build())
}

internal fun LexiconType.toTypeName(environment: LexiconEnvironment, context: LexiconContext): TypeName {
    return when (this) {
        is BlobField -> TypeNames.Blob
        is BooleanField -> Boolean::class.asTypeName()
        is BytesField -> ByteArray::class.asTypeName()
        is CidLinkField -> URI::class.asTypeName()
        is IntegerField -> Int::class.asTypeName()
        is StringField -> getTypeName()
        is ObjectField, is UnknownField -> JsonObject::class.asTypeName()
        is RefField -> {
            handleRefGeneration(context, environment)
            val ref = LexiconRef(ref)
            val lexiconType = environment.loadReference(context.lexicon, ref)
            if (lexiconType is ObjectField) {
                if (ref.schemaId.isBlank()) {
                    ClassName(packageName = context.authority, ref.objectRef.titleCaseFirstChar())
                } else if (ref.objectRef.isBlank()) {
                    ClassName(
                        packageName = ref.schemaId,
                        ref.schemaId.split('.').last().titleCaseFirstChar()
                    )
                } else {
                    ClassName(packageName = ref.schemaId, ref.objectRef.titleCaseFirstChar())
                }
            } else if (lexiconType is RecordField) {
                val lexicon = environment.loadLexicon(ref.schemaId)
                ClassName(
                    lexicon.id,
                    lexicon.id.split('.').last().titleCaseFirstChar()
                )
            } else {
                require(lexiconType !is RefField) { "References cannot reference another reference" }
                require(lexiconType !is UnionField) { "References cannot reference a union" }
                lexiconType.toTypeName(environment, context)
            }
        }
        is UnionField -> {
            generateClass(this, context, environment)
            ClassName(context.authority, "${context.name}Union")
        }
        else -> throw GradleException("Unable to parameterize array with $this")
    }
}

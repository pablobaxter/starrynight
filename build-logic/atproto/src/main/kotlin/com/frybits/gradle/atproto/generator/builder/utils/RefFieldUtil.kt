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

package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.generator.builder.generateClass
import com.frybits.gradle.atproto.generator.builder.generateDataClass
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.categories.ArrayField
import com.frybits.gradle.atproto.lexicon.categories.BlobField
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.frybits.gradle.atproto.lexicon.categories.CidLinkField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField
import com.frybits.gradle.atproto.utils.LexiconRef
import com.frybits.gradle.atproto.utils.titleCaseFirstChar
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.json.JsonObject
import java.net.URI

internal fun FunSpec.Builder.handleParam(name: String, lexiconType: RefField, isRequired: Boolean, isNullable: Boolean, context: LexiconContext, environment: LexiconEnvironment) {
    val ref = LexiconRef(lexiconType.ref)
    val lexicon = if (ref.schemaId.isBlank()) {
        context.lexicon
    } else {
        environment.loadLexicon(ref.schemaId)
    }
    val typeName = when (val refLexiconType = environment.loadReference(lexicon, ref)) {
        is IntegerField -> Int::class.asTypeName()
        is BlobField -> TypeNames.Blob
        is BooleanField -> Boolean::class.asTypeName()
        is BytesField -> ByteArray::class.asTypeName()
        is CidLinkField -> URI::class.asTypeName()
        is StringField -> refLexiconType.getTypeName()
        is ArrayField -> List::class.asTypeName().parameterizedBy(refLexiconType.parameterizedType(environment, context))
        is ObjectField -> ClassName(lexicon.id, ref.objectRef.ifBlank { lexicon.id.split('.').last() }.titleCaseFirstChar())
        is UnknownField -> JsonObject::class.asTypeName()
        else -> throw IllegalArgumentException("Type ${refLexiconType::class} cannot be used with a reference")
    }
    val parameter = ParameterSpec.builder(name, typeName)
    parameter.handleDescription(lexiconType)

    if (!isRequired) {
        if (isNullable) {
            parameter.defaultValue("%L", null)
        }
    }

    addParameter(parameter.build())
}

internal fun TypeSpec.Builder.handleProperty(name: String, lexiconType: RefField, context: LexiconContext, environment: LexiconEnvironment) {
    val ref = LexiconRef(lexiconType.ref)
    val lexicon = if (ref.schemaId.isBlank()) {
        context.lexicon
    } else {
        environment.loadLexicon(ref.schemaId)
    }
    val typeName = when (val refLexiconType = environment.loadReference(lexicon, ref)) {
        is IntegerField -> Int::class.asTypeName()
        is BlobField -> TypeNames.Blob
        is BooleanField -> Boolean::class.asTypeName()
        is BytesField -> ByteArray::class.asTypeName()
        is CidLinkField -> URI::class.asTypeName()
        is StringField -> refLexiconType.getTypeName()
        is ArrayField -> List::class.asTypeName().parameterizedBy(refLexiconType.parameterizedType(environment, context))
        is ObjectField -> ClassName(lexicon.id, ref.objectRef.ifBlank { lexicon.id.split('.').last() }.titleCaseFirstChar())
        is UnknownField -> JsonObject::class.asTypeName()
        else -> throw IllegalArgumentException("Type ${refLexiconType::class} cannot be used with a reference")
    }
    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    property.handleDescription(lexiconType)

    addProperty(property.build())
}

internal fun RefField.handleRefGeneration(context: LexiconContext, environment: LexiconEnvironment) {
    val ref = LexiconRef(ref)
    val lexicon = if (ref.schemaId.isBlank()) {
        context.lexicon
    } else {
        environment.loadLexicon(ref.schemaId)
    }
    when (val lexiconType = environment.loadReference(lexicon, ref)) {
        is ObjectField -> generateDataClass(lexiconType, LexiconContext(ref.objectRef.titleCaseFirstChar(), lexicon), environment)
        is RecordField -> generateClass(lexiconType, LexiconContext(ref.schemaId.split('.').last().titleCaseFirstChar(), lexicon), environment)
        else -> Unit
    }
}

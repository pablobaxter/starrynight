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

package com.frybits.gradle.atproto.generator

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
import com.frybits.gradle.atproto.lexicon.categories.StringFormat
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField
import com.frybits.gradle.atproto.utils.titleCaseFirstChar
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.annotated
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.gradle.api.GradleException
import java.net.URI
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun ArrayField.generateField(
    name: String,
    className: ClassName,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    initCodeBlockBuilder: CodeBlock.Builder,
    companionBuilder: TypeSpec.Builder,
    isRequired: Boolean,
    isNullable: Boolean,
    rkeyMap: Map<String, ClassName>,
    toGenerateCollector: MutableSet<String>
) {
    val listTypeName = List::class.asTypeName()

    val parameterizedType = when (items) {
        is BlobField -> ClassName(packageName = "com.frybits.starrynight.atproto.models.blob", "Blob")
        is BooleanField -> Boolean::class.asTypeName()
        is BytesField -> ByteArray::class.asTypeName()
        is CidLinkField -> URI::class.asTypeName()
        is IntegerField -> Int::class.asTypeName()
        is StringField -> {
            val stringPackage = "com.frybits.starrynight.atproto.models.strings"
            when (items.format) {
                StringFormat.AT_IDENTIFIER -> ClassName(stringPackage, "ATIdentifier")
                StringFormat.DATETIME -> Instant::class.asTypeName()
                    .annotated(AnnotationSpec.builder(Serializable::class).addMember("%T::class", ClassName("com.frybits.starrynight.atproto.serializers", "DateTimeSerializer")).build())
                StringFormat.DID -> ClassName(stringPackage, "Did")
                StringFormat.HANDLE -> ClassName(stringPackage, "Handle")
                StringFormat.NSID -> ClassName(stringPackage, "Nsid")
                StringFormat.TID -> ClassName(stringPackage, "Tid")
                StringFormat.RECORD_KEY -> ClassName(stringPackage, "RecordKey")
                StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> URI::class.asTypeName()
                    .annotated(AnnotationSpec.builder(Serializable::class).addMember("%T::class", ClassName("com.frybits.starrynight.atproto.serializers", "URISerializer")).build())
                StringFormat.LANGUAGE -> ClassName(stringPackage, "Language")
                null -> String::class.asTypeName()
            }
        }
        is ObjectField -> JsonObject::class.asTypeName()
        is RefField -> {
            val packageName = items.ref.substringBefore('#')
            val type = items.ref.substringAfter('#', missingDelimiterValue = "")
            if (packageName.isBlank()) {
                toGenerateCollector.add("${className.packageName}#$type")
                ClassName(packageName = className.packageName, type.titleCaseFirstChar())
            } else if (type.isBlank()) {
                toGenerateCollector.add(packageName)
                ClassName(packageName = packageName, packageName.split('.').last().titleCaseFirstChar())
            } else {
                toGenerateCollector.add(items.ref)
                ClassName(packageName = packageName, type.titleCaseFirstChar())
            }
        }
        is UnionField -> {
            val nestedClassName = className.nestedClass("${name.titleCaseFirstChar()}Union")
            typeSpecBuilder.addType(items.generateUnionFieldInterface(
                typeName = nestedClassName,
                toGenerateCollector = toGenerateCollector
            ))
            nestedClassName
        }
        is UnknownField -> JsonObject::class.asTypeName()
        is RecordField -> requireNotNull(rkeyMap[items.key]) { "Record key ${items.key} not found" }
        else -> throw GradleException("Unable to parameterize array with $items")
    }

    val typeName = listTypeName.parameterizedBy(parameterizedType).copy(nullable = isNullable)

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

    if (!isRequired) {
        if (isNullable) {
            parameter.defaultValue("%L", null)
        } else {
            parameter.defaultValue("emptyList()")
        }
    }

    val outerCodeBlock = CodeBlock.builder()
    val innerCodeBlock = CodeBlock.builder()

    if (initCodeBlockBuilder.isNotEmpty()) {
        outerCodeBlock.addStatement("")
    }

    outerCodeBlock.addStatement("// Begin $name requirements")
    if (isNullable) {
        outerCodeBlock.beginControlFlow("if (%L != null)", name)
    }
    if (minLength != null) {
        innerCodeBlock.addStatement("require(%L.size >= %L) { %P }", name, minLength, $$"Expected $$name size to be greater than or equal to $$minLength. Currently ${$$name.size}")
    }
    if (maxLength != null) {
        innerCodeBlock.addStatement("require(%L.size <= %L) { %P }", name, maxLength, $$"Expected $$name size to be less than or equal to $$maxLength. Currently ${$$name.size}")
    }
    if (innerCodeBlock.isNotEmpty()) {
        outerCodeBlock.add(innerCodeBlock.build())
    }
    if (isNullable) {
        outerCodeBlock.endControlFlow()
    }
    outerCodeBlock.addStatement("// End $name requirements")

    if (innerCodeBlock.isNotEmpty()) {
        initCodeBlockBuilder.add(outerCodeBlock.build())
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

internal fun ObjectField.generateNestedField(
    name: String,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    isRequired: Boolean,
    isNullable: Boolean
) {

    val typeName = JsonObject::class.asTypeName().copy(nullable = isNullable)

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

    if (!isRequired) {
        if (isNullable) {
            parameter.defaultValue("%L", null)
        } else {
            parameter.defaultValue("%T(%L)", JsonObject::class, "emptyMap()")
        }
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

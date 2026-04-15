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

import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.gradle.internal.extensions.stdlib.capitalized

internal fun RefField.generateField(
    name: String,
    className: ClassName,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    isRequired: Boolean,
    isNullable: Boolean,
    toGenerateCollector: MutableSet<String>
) {
    val packageName = ref.substringBefore('#')
    val type = ref.substringAfter('#', missingDelimiterValue = "")
    val typeName = when {
        packageName.isBlank() -> {
            toGenerateCollector.add("${className.packageName}#$type")
            ClassName(packageName = className.packageName, type.capitalized())
        }

        type.isBlank() -> {
            toGenerateCollector.add(packageName)
            ClassName(packageName = packageName, packageName.split('.').last().capitalized())
        }

        else -> {
            toGenerateCollector.add(ref)
            ClassName(packageName = packageName, type.capitalized())
        }
    }.copy(nullable = isNullable)

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    if (!isRequired) {
        if (isNullable) {
            parameter.defaultValue("%L", null)
        }
    }

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

internal fun UnionField.generateField(
    name: String,
    className: ClassName,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    isRequired: Boolean,
    isNullable: Boolean,
    toGenerateCollector: MutableSet<String>
) {
    val typeName = className.nestedClass("${name.capitalized()}Union")

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
        }
    }

    typeSpecBuilder.addType(generateUnionFieldInterface(className.nestedClass("${name.capitalized()}Union"), toGenerateCollector))

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

internal fun UnionField.generateUnionFieldInterface(
    typeName: ClassName,
    toGenerateCollector: MutableSet<String>
): TypeSpec {
    val unionTypeSpec = TypeSpec.interfaceBuilder(typeName)
        .addAnnotation(Serializable::class)
        .addModifiers(KModifier.PUBLIC, KModifier.SEALED)

    val unionFields = refs.map { ref ->
        val internalPackageName = ref.substringBefore('#')
        val internalType = ref.substringAfter('#', missingDelimiterValue = "")
        val fullRefName = if (internalPackageName.isBlank()) {
            "${typeName.packageName}#$internalType"
        } else if (internalType.isBlank()) {
            internalPackageName
        } else {
            ref
        }

        toGenerateCollector.add(fullRefName)

        val refTypeName = if (internalPackageName.isBlank()) {
            ClassName(packageName = typeName.packageName, internalType.capitalized())
        } else if (internalType.isBlank()) {
            ClassName(packageName = internalPackageName, internalPackageName.split('.').last().capitalized())
        } else {
            ClassName(packageName = internalPackageName, internalType.capitalized())
        }

        val refName = "${internalType.capitalized()}UnionField"
        val unionFieldClassName = ClassName(typeName.packageName, refName)
        return@map TypeSpec.classBuilder(unionFieldClassName)
            .addAnnotation(Serializable::class)
            .addAnnotation(JvmInline::class)
            .addAnnotation(AnnotationSpec.builder(SerialName::class).addMember("%S", fullRefName).build())
            .addModifiers(KModifier.PUBLIC, KModifier.VALUE)
            .addSuperinterface(typeName)
            .primaryConstructor(
                FunSpec.constructorBuilder().addParameter(
                    ParameterSpec.builder("prop", refTypeName).build()
                ).build()
            )
            .addProperty(
                PropertySpec.builder("prop", refTypeName)
                    .initializer("prop")
                    .build()
            ).build()
    }

    unionTypeSpec.addTypes(unionFields)

    if (description != null) {
        unionTypeSpec.addKdoc(description)
    }

    return unionTypeSpec.build()
}

internal fun UnknownField.generateField(
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

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
import com.frybits.gradle.atproto.lexicon.categories.ParamsField
import com.frybits.gradle.atproto.lexicon.categories.ProcedureField
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.Serializable
import org.gradle.api.GradleException
import org.gradle.internal.extensions.stdlib.capitalized

internal fun RecordField.generateClass(
    className: ClassName,
    toGenerateCollector: MutableSet<String>,
    rkeyMap: Map<String, ClassName>,
    fileSpecCollector: MutableList<FileSpec>
) {
    val fileBuilder = FileSpec.builder(className)

    fileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")

    val typeSpecBuilder = TypeSpec.classBuilder(className)
        .addAnnotation(Serializable::class)
        .addModifiers(KModifier.PUBLIC)

    if (description != null) {
        typeSpecBuilder.addKdoc(description)
    }

    val constructor = FunSpec.constructorBuilder()
    val initCodeBlock = CodeBlock.builder()
    val companion = TypeSpec.companionObjectBuilder()

    require(record is ObjectField)
    record.generateClass(
        className = className,
        typeSpecBuilder = typeSpecBuilder,
        constructor = constructor,
        initCodeBlock = initCodeBlock,
        companion = companion,
        toGenerateCollector = toGenerateCollector,
        rkeyMap = rkeyMap
    )

    if (constructor.parameters.isNotEmpty()) {
        typeSpecBuilder.primaryConstructor(constructor.build())
    }

    if (initCodeBlock.isNotEmpty()) {
        typeSpecBuilder.addInitializerBlock(initCodeBlock.build())
    }

    if (companion.typeSpecs.isNotEmpty() || companion.propertySpecs.isNotEmpty()) {
        typeSpecBuilder.addType(companion.build())
    }

    if (typeSpecBuilder.propertySpecs.isNotEmpty()) {
        typeSpecBuilder.addModifiers(KModifier.DATA)
    }

    fileSpecCollector.add(fileBuilder.addType(typeSpecBuilder.build()).build())
}

internal fun ProcedureField.generateClass(
    className: ClassName,
    id: String,
    toGenerateCollector: MutableSet<String>,
    rkeyMap: Map<String, ClassName>,
    fileSpecCollector: MutableList<FileSpec>
) {
    val name = id.split('.').last()
    val fileBuilder = FileSpec.builder(className)

    fileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")

    val typeSpecBuilder = TypeSpec.interfaceBuilder(className)
        .addModifiers(KModifier.PUBLIC)

    if (description != null) {
        typeSpecBuilder.addKdoc(description)
    }

    val procedureFunSpecBuilder = FunSpec.builder(name)
        .addModifiers(KModifier.PUBLIC, KModifier.SUSPEND, KModifier.ABSTRACT)

    val resultTypeName = ClassName("retrofit2", "Response")

    if (output != null) {
        val outputClassName = when (val schema = output.schema) {
            is ObjectField -> {
                val cn = ClassName(id, "${name.capitalized()}Response")
                val outputTypeSpec = TypeSpec.classBuilder(cn)
                    .addAnnotation(Serializable::class)
                    .addModifiers(KModifier.PUBLIC)

                if (schema.description != null) {
                    outputTypeSpec.addKdoc(schema.description)
                }

                val constructor = FunSpec.constructorBuilder()
                val initCodeBlock = CodeBlock.builder()
                val companion = TypeSpec.companionObjectBuilder()
                schema.generateClass(
                    className = cn,
                    typeSpecBuilder = outputTypeSpec,
                    constructor = constructor,
                    initCodeBlock = initCodeBlock,
                    companion = companion,
                    toGenerateCollector = toGenerateCollector,
                    rkeyMap = rkeyMap
                )

                if (constructor.parameters.isNotEmpty()) {
                    outputTypeSpec.primaryConstructor(constructor.build())
                }

                if (initCodeBlock.isNotEmpty()) {
                    outputTypeSpec.addInitializerBlock(initCodeBlock.build())
                }

                if (companion.typeSpecs.isNotEmpty() || companion.propertySpecs.isNotEmpty()) {
                    outputTypeSpec.addType(companion.build())
                }

                if (outputTypeSpec.propertySpecs.isNotEmpty()) {
                    outputTypeSpec.addModifiers(KModifier.DATA)
                }
                val outputFileBuilder = FileSpec.builder(cn)
                outputFileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")
                outputFileBuilder.addType(outputTypeSpec.build())
                fileSpecCollector.add(outputFileBuilder.build())
                cn
            }
            is RefField -> {
                val packageName = schema.ref.substringBefore('#')
                val refName = schema.ref.substringAfter('#', missingDelimiterValue = "")
                if (packageName.isBlank()) {
                    toGenerateCollector.add("$id#$refName")
                    ClassName(id, refName)
                } else if (refName.isBlank()) {
                    toGenerateCollector.add(packageName)
                    ClassName(packageName, packageName.split('.').last().capitalized())
                } else {
                    toGenerateCollector.add(schema.ref)
                    ClassName(packageName, refName)
                }
            }
            is UnionField -> {
                val cn = ClassName(id, "${name.capitalized()}ResponseUnion")
                val unionTypeSpec = schema.generateUnionFieldInterface(
                    typeName = cn,
                    toGenerateCollector = toGenerateCollector
                )
                val unionFileBuilder = FileSpec.builder(cn)
                unionFileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")
                unionFileBuilder.addType(unionTypeSpec)
                fileSpecCollector.add(unionFileBuilder.build())
                cn
            }
            else -> null
        }

        procedureFunSpecBuilder.returns(resultTypeName.parameterizedBy(outputClassName ?: ClassName("okhttp3", "ResponseBody")), output.description?.let { CodeBlock.of(it) } ?: CodeBlock.builder().build())
    } else {
        procedureFunSpecBuilder.returns(resultTypeName.parameterizedBy(Unit::class.asTypeName()))
    }

    if (parameters != null) {
        require(parameters is ParamsField)
        val hardCodedQueryParams = hashMapOf<String, String>()
        parameters.description?.let { procedureFunSpecBuilder.addKdoc(it) }
        val required = parameters.required.orEmpty().toSet()
        parameters.properties.forEach { (paramName, type) ->
            val isRequired = paramName in required
            val paramTypeName = when (type) {
                is BooleanField -> Boolean::class.asTypeName()
                is IntegerField -> Int::class.asTypeName()
                is StringField -> String::class.asTypeName()
                else -> throw GradleException("The following type cannot be part of a procedure parameter: $type")
            }.copy(nullable = !isRequired)

            val paramSpec = ParameterSpec.builder(paramName, paramTypeName)

            when (type) {
                is BooleanField -> {
                    if (type.description != null) {
                        paramSpec.addKdoc(type.description)
                    }
                    if (type.const != null) {
                        hardCodedQueryParams[paramName] = type.const.toString()
                    } else {
                        paramSpec.addAnnotation(AnnotationSpec.builder(ClassName("retrofit2.http", "Query")).addMember("%S", paramName).build())
                        if (type.default != null) {
                            paramSpec.defaultValue("%L", type.default)
                        }
                    }
                }
                is IntegerField -> {
                    if (type.description != null) {
                        paramSpec.addKdoc(type.description)
                    }
                    if (type.const != null) {
                        hardCodedQueryParams[paramName] = type.const.toString()
                    } else {
                        paramSpec.addAnnotation(AnnotationSpec.builder(ClassName("retrofit2.http", "Query")).addMember("%S", paramName).build())
                        if (type.default != null) {
                            paramSpec.defaultValue("%L", type.default)
                        }
                    }
                }
                is StringField -> {
                    if (type.description != null) {
                        paramSpec.addKdoc(type.description)
                    }
                    if (type.const != null) {
                        hardCodedQueryParams[paramName] = type.const
                    } else {
                        paramSpec.addAnnotation(AnnotationSpec.builder(ClassName("retrofit2.http", "Query")).addMember("%S", paramName).build())
                        if (type.default != null) {
                            paramSpec.defaultValue("%S", type.default)
                        }
                    }
                }
                else -> throw GradleException("The following type cannot be part of a procedure parameter: $type")
            }

            val memberString = if (hardCodedQueryParams.isNotEmpty()) {
                "${id}?${hardCodedQueryParams.map { (n, v) -> "$n=$v" }.joinToString(",")}"
            } else {
                id
            }

            procedureFunSpecBuilder.addAnnotation(AnnotationSpec.builder(ClassName("retrofit2.http", "POST")).addMember("%S", memberString).build())
            procedureFunSpecBuilder.addParameter(paramSpec.build())
        }
    }

    if (input != null) {
        val inputClassName = when (val schema = input.schema) {
            is ObjectField -> {
                val cn = ClassName(id, "${name.capitalized()}Request")
                val inputTypeSpec = TypeSpec.classBuilder(cn)
                    .addAnnotation(Serializable::class)
                    .addModifiers(KModifier.PUBLIC)

                if (schema.description != null) {
                    inputTypeSpec.addKdoc(schema.description)
                }

                val constructor = FunSpec.constructorBuilder()
                val initCodeBlock = CodeBlock.builder()
                val companion = TypeSpec.companionObjectBuilder()
                schema.generateClass(
                    className = cn,
                    typeSpecBuilder = inputTypeSpec,
                    constructor = constructor,
                    initCodeBlock = initCodeBlock,
                    companion = companion,
                    toGenerateCollector = toGenerateCollector,
                    rkeyMap = rkeyMap
                )

                if (constructor.parameters.isNotEmpty()) {
                    inputTypeSpec.primaryConstructor(constructor.build())
                }

                if (initCodeBlock.isNotEmpty()) {
                    inputTypeSpec.addInitializerBlock(initCodeBlock.build())
                }

                if (companion.typeSpecs.isNotEmpty() || companion.propertySpecs.isNotEmpty()) {
                    inputTypeSpec.addType(companion.build())
                }

                if (inputTypeSpec.propertySpecs.isNotEmpty()) {
                    inputTypeSpec.addModifiers(KModifier.DATA)
                }
                val inputFileBuilder = FileSpec.builder(cn)
                inputFileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")
                inputFileBuilder.addType(inputTypeSpec.build())
                fileSpecCollector.add(inputFileBuilder.build())
                cn
            }
            is RefField -> {
                val (packageName, refName) = schema.ref.split('#')
                if (packageName.isBlank()) {
                    toGenerateCollector.add("$id#$refName")
                    ClassName(id, refName)
                } else {
                    toGenerateCollector.add(schema.ref)
                    ClassName(packageName, refName)
                }
            }
            is UnionField -> {
                val cn = ClassName(id, "${name.capitalized()}RequestUnion")
                val unionTypeSpec = schema.generateUnionFieldInterface(
                    typeName = cn,
                    toGenerateCollector = toGenerateCollector
                )
                val unionFileBuilder = FileSpec.builder(cn)
                unionFileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")
                unionFileBuilder.addType(unionTypeSpec)
                fileSpecCollector.add(unionFileBuilder.build())
                cn
            }
            else -> null
        }

        val paramSpec = ParameterSpec.builder("requestBody", inputClassName ?: ClassName("okhttp3", "ResponseBody"))
            .addAnnotation(ClassName("retrofit2.http", "Body"))

        if (input.description != null) {
            paramSpec.addKdoc(input.description)
        }

        procedureFunSpecBuilder.addParameter(paramSpec.build())
    }

    // TODO Handle errors

    typeSpecBuilder.addFunction(procedureFunSpecBuilder.build())
    fileSpecCollector.add(fileBuilder.addType(typeSpecBuilder.build()).build())
}

private fun ObjectField.generateClass(
    className: ClassName,
    typeSpecBuilder: TypeSpec.Builder,
    constructor: FunSpec.Builder,
    initCodeBlock: CodeBlock.Builder,
    companion: TypeSpec.Builder,
    toGenerateCollector: MutableSet<String>,
    rkeyMap: Map<String, ClassName>
) {
    val required = required.orEmpty().toSet()
    val nullable = nullable.orEmpty().toSet()

    properties.forEach { (name, type) ->
        val isRequired = name in required
        val isNullable = name in nullable
        when (type) {
            is BlobField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    initCodeBlockBuilder = initCodeBlock,
                    isRequired = isRequired,
                    isNullable = isNullable
                )
            }
            is BooleanField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    companionBuilder = companion,
                    isRequired = isRequired,
                    isNullable = isNullable
                )
            }
            is BytesField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    initCodeBlockBuilder = initCodeBlock,
                    isRequired = isRequired,
                    isNullable = isNullable
                )
            }
            is IntegerField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    initCodeBlockBuilder = initCodeBlock,
                    companionBuilder = companion,
                    isRequired = isRequired,
                    isNullable = isNullable
                )
            }
            is StringField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    initCodeBlockBuilder = initCodeBlock,
                    companionBuilder = companion,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    toGenerateCollector = toGenerateCollector
                )
            }
            is CidLinkField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    isRequired = isRequired,
                    isNullable = isNullable
                )
            }
            is ArrayField -> {
                type.generateField(
                    name = name,
                    className = className,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    initCodeBlockBuilder = initCodeBlock,
                    companionBuilder = companion,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    rkeyMap = rkeyMap,
                    toGenerateCollector = toGenerateCollector
                )
            }
            is ObjectField -> {
                type.generateNestedField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    isRequired = isRequired,
                    isNullable = isNullable
                )
            }
            is RefField -> {
                type.generateField(
                    name = name,
                    className = className,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    toGenerateCollector = toGenerateCollector
                )
            }
            is UnionField -> {
                type.generateField(
                    name = name,
                    className = className,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    toGenerateCollector = toGenerateCollector
                )
            }
            is UnknownField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    isRequired = isRequired,
                    isNullable = isNullable
                )
            }
            else -> throw GradleException("Type not supported yet. Name=$name, type=$type")
        }
    }
}
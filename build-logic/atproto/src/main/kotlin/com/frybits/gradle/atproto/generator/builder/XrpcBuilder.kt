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

package com.frybits.gradle.atproto.generator.builder

import com.frybits.gradle.atproto.generator.builder.utils.TypeNames
import com.frybits.gradle.atproto.generator.builder.utils.createFileBuilder
import com.frybits.gradle.atproto.generator.builder.utils.handleDescription
import com.frybits.gradle.atproto.generator.builder.utils.handleRefGeneration
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.categories.ArrayField
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.HttpField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.ParamsField
import com.frybits.gradle.atproto.lexicon.categories.ProcedureField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.utils.LexiconRef
import com.frybits.gradle.atproto.utils.lowerCaseFirstChar
import com.frybits.gradle.atproto.utils.titleCaseFirstChar
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

internal fun generateClass(lexiconType: HttpField, context: LexiconContext, environment: LexiconEnvironment) {
    val className = ClassName(context.authority, "${context.name}Api")
    val fileBuilder = createFileBuilder(className)

    val typeSpecBuilder = TypeSpec.interfaceBuilder(className)
        .addModifiers(KModifier.PUBLIC)
        .handleDescription(lexiconType)

    val procedureFunSpecBuilder = FunSpec.builder(context.name.lowerCaseFirstChar())
        .addModifiers(KModifier.PUBLIC, KModifier.SUSPEND, KModifier.ABSTRACT)

    val output = lexiconType.output
    if (output != null) {
        val outputClassName = when (val schema = output.schema) {
            is ObjectField -> {
                val outputContext = LexiconContext("${context.name}Response", context.lexicon)
                generateDataClass(schema, outputContext, environment)
                ClassName(outputContext.authority, outputContext.name)
            }
            is RefField -> {
                schema.handleRefGeneration(context, environment)
                val ref = LexiconRef(schema.ref)
                if (ref.schemaId.isNotBlank()) {
                    ClassName(ref.schemaId, ref.objectRef.ifBlank { ref.schemaId.split('.').last() }.titleCaseFirstChar())
                } else {
                    ClassName(context.authority, ref.objectRef.titleCaseFirstChar())
                }
            }
            is UnionField -> {
                generateClass(schema, context, environment)
                ClassName(context.authority, "${context.name}Union")
            }
            else -> null
        }

        procedureFunSpecBuilder.returns(TypeNames.RetrofitResponse.parameterizedBy(outputClassName ?: TypeNames.OkHttpResponseBody), output.description?.let { CodeBlock.of(it) } ?: CodeBlock.builder().build())
    } else {
        procedureFunSpecBuilder.returns(TypeNames.RetrofitResponse.parameterizedBy(Unit::class.asTypeName()))
    }

    val hardCodedQueryParams = hashMapOf<String, String>()

    val parameters = lexiconType.parameters
    if (parameters != null) {
        require(parameters is ParamsField)
        procedureFunSpecBuilder.handleDescription(parameters)
        val required = parameters.required.orEmpty().toSet()
        parameters.properties.forEach { (paramName, type) ->
            val isRequired = paramName in required
            val paramTypeName = when (type) {
                is BooleanField -> Boolean::class.asTypeName()
                is IntegerField -> Int::class.asTypeName()
                is StringField -> String::class.asTypeName()
                is ArrayField -> {
                    val parametrizedType = when (type.items) {
                        is BooleanField -> Boolean::class.asTypeName()
                        is IntegerField -> Int::class.asTypeName()
                        is StringField -> String::class.asTypeName()
                        else -> throw IllegalArgumentException("The following type cannot be part of a procedure parameter: ${type.items}")
                    }
                    List::class.asTypeName().parameterizedBy(parametrizedType)
                }
                else -> throw IllegalArgumentException("The following type cannot be part of a procedure parameter: $type")
            }.copy(nullable = !isRequired)

            val paramSpec = ParameterSpec.builder(paramName, paramTypeName)
            paramSpec.handleDescription(type)

            when (type) {
                is BooleanField -> {
                    if (type.const != null) {
                        hardCodedQueryParams[paramName] = type.const.toString()
                    } else {
                        paramSpec.addAnnotation(AnnotationSpec.builder(TypeNames.RetrofitQuery).addMember("%S", paramName).build())
                        if (type.default != null) {
                            paramSpec.defaultValue("%L", type.default)
                        }
                    }
                }
                is IntegerField -> {
                    if (type.const != null) {
                        hardCodedQueryParams[paramName] = type.const.toString()
                    } else {
                        paramSpec.addAnnotation(AnnotationSpec.builder(TypeNames.RetrofitQuery).addMember("%S", paramName).build())
                        if (type.default != null) {
                            paramSpec.defaultValue("%L", type.default)
                        }
                    }
                }
                is StringField -> {
                    if (type.const != null) {
                        hardCodedQueryParams[paramName] = type.const
                    } else {
                        paramSpec.addAnnotation(AnnotationSpec.builder(TypeNames.RetrofitQuery).addMember("%S", paramName).build())
                        if (type.default != null) {
                            paramSpec.defaultValue("%S", type.default)
                        }
                    }
                }
                is ArrayField -> {
                    paramSpec.addAnnotation(AnnotationSpec.builder(TypeNames.RetrofitQuery).addMember("%S", paramName).build())
                }
                else -> throw IllegalStateException("The following type cannot be part of a procedure parameter: $type")
            }

            procedureFunSpecBuilder.addParameter(paramSpec.build())
        }
    }

    val memberString = if (hardCodedQueryParams.isNotEmpty()) {
        "${context.authority}?${hardCodedQueryParams.map { (n, v) -> "$n=$v" }.joinToString(",")}"
    } else {
        context.authority
    }

    procedureFunSpecBuilder.addAnnotation(AnnotationSpec.builder(TypeNames.RetrofitPost).addMember("%S", memberString).build())

    if (lexiconType is ProcedureField) {
        val input = lexiconType.input
        if (input != null) {
            val inputClassName = when (val schema = input.schema) {
                is ObjectField -> {
                    val inputContext = LexiconContext("${context.name}Request", context.lexicon)
                    generateDataClass(schema, inputContext, environment)
                    ClassName(inputContext.authority, inputContext.name)
                }

                is RefField -> {
                    schema.handleRefGeneration(context, environment)
                    val ref = LexiconRef(schema.ref)
                    if (ref.schemaId.isNotBlank()) {
                        ClassName(ref.schemaId,
                            ref.objectRef.ifBlank { ref.schemaId.split('.').last() }
                                .titleCaseFirstChar()
                        )
                    } else {
                        ClassName(context.authority, ref.objectRef.titleCaseFirstChar())
                    }
                }

                is UnionField -> {
                    generateClass(schema, context, environment)
                    ClassName(context.authority, "${context.name}Union")
                }

                else -> null
            }
            val inputParamSpec =
                ParameterSpec.builder("requestBody", inputClassName ?: TypeNames.OkHttpRequestBody)
                    .addAnnotation(TypeNames.RetrofitBody)

            input.schema?.let(inputParamSpec::handleDescription)

            procedureFunSpecBuilder.addParameter(inputParamSpec.build())
        }
    }

    typeSpecBuilder.addFunction(procedureFunSpecBuilder.build())
    fileBuilder.addType(typeSpecBuilder.build())

    val fileSpec = fileBuilder.build()

    environment.generateFile(fileSpec)
}

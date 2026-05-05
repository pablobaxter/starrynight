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

import com.frybits.gradle.atproto.generator.builder.utils.createFileBuilder
import com.frybits.gradle.atproto.generator.builder.utils.handleConst
import com.frybits.gradle.atproto.generator.builder.utils.handleDescription
import com.frybits.gradle.atproto.generator.builder.utils.handleParam
import com.frybits.gradle.atproto.generator.builder.utils.handleProperty
import com.frybits.gradle.atproto.generator.builder.utils.handleRefGeneration
import com.frybits.gradle.atproto.generator.builder.utils.handleRequirements
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
import com.frybits.gradle.atproto.lexicon.categories.TokenField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField
import com.frybits.gradle.atproto.utils.LexiconRef
import com.frybits.gradle.atproto.utils.titleCaseFirstChar
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal fun generateClass(lexiconType: UnionField, context: LexiconContext, environment: LexiconEnvironment) {
    val className = ClassName(context.authority, "${context.name}Union")
    val fileBuilder = createFileBuilder(className)

    val typeSpec = TypeSpec.interfaceBuilder(className)
        .addAnnotation(Serializable::class)
        .addModifiers(KModifier.PUBLIC, KModifier.SEALED)

    typeSpec.handleDescription(lexiconType)

    lexiconType.refs.forEach { reference ->
        val ref = LexiconRef(reference)

        val lexicon = if (ref.schemaId.isBlank()) {
            context.lexicon
        } else {
            environment.loadLexicon(ref.schemaId)
        }

        val refLexiconType = environment.loadReference(lexicon, ref)

        when (val refLexiconType = environment.loadReference(lexicon, ref)) {
            is ObjectField -> generateDataClass(refLexiconType, LexiconContext(ref.objectRef.ifBlank { ref.schemaId.split('.').last() }.titleCaseFirstChar(), lexicon), environment)
            is RecordField -> generateClass(refLexiconType, LexiconContext(ref.schemaId.split('.').last().titleCaseFirstChar(), lexicon), environment)
            else -> return@forEach
        }

        val refClassName = if (refLexiconType is ObjectField) {
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
        } else if (refLexiconType is RecordField) {
            val lexicon = environment.loadLexicon(ref.schemaId)
            ClassName(
                lexicon.id,
                lexicon.id.split('.').last().titleCaseFirstChar()
            )
        } else {
            return@forEach
        }


        val unionFieldClassName = ClassName(context.authority, "${refClassName.simpleName}UnionField")

        val unionTypeSpec = TypeSpec.classBuilder(unionFieldClassName)
            .addAnnotation(Serializable::class)
            .addAnnotation(JvmInline::class)
            .addAnnotation(AnnotationSpec.builder(SerialName::class).addMember("%S", ref).build())
            .addModifiers(KModifier.PUBLIC, KModifier.VALUE)
            .addSuperinterface(className)
            .primaryConstructor(
                FunSpec.constructorBuilder().addParameter(
                    ParameterSpec.builder("prop", refClassName).build()
                ).build()
            )
            .addProperty(
                PropertySpec.builder("prop", refClassName)
                    .initializer("prop")
                    .build()
            ).build()

        typeSpec.addType(unionTypeSpec)
    }

    fileBuilder.addType(typeSpec.build())

    val fileSpec = fileBuilder.build()

    environment.generateFile(fileSpec)
}

internal fun generateClass(lexiconType: RecordField, context: LexiconContext, environment: LexiconEnvironment) {
    val className = ClassName(context.authority, context.name)
    val fileBuilder = createFileBuilder(className)
    fileBuilder.handleDescription(lexiconType)
    generateDataClass(lexiconType.record as ObjectField, context, environment, fileBuilder)
}

internal fun generateDataClass(lexiconType: ObjectField, context: LexiconContext, environment: LexiconEnvironment, fileBuilder: FileSpec.Builder? = null) {
    val className = ClassName(context.authority, context.name)
    val dataFileBuilder = fileBuilder ?: createFileBuilder(className)

    val typeSpecBuilder = TypeSpec.classBuilder(className)
        .addAnnotation(Serializable::class)
        .addModifiers(KModifier.PUBLIC, KModifier.DATA)

    typeSpecBuilder.handleDescription(lexiconType)

    val constructorBuilder = FunSpec.constructorBuilder()
    val companionBuilder = TypeSpec.companionObjectBuilder()
    val initCodeBlockBuilder = CodeBlock.builder()

    val required = lexiconType.required.orEmpty().toSet()
    val nullable = lexiconType.nullable.orEmpty().toSet()

    lexiconType.properties.forEach { (name, type) ->
        val isRequired = name in required
        val isNullable = name in nullable
        when (type) {
            is BooleanField -> {
                if (type.const != null) {
                    companionBuilder.handleConst(
                        name = name,
                        lexiconType = type
                    )
                    return@forEach
                }
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )
            }
            is IntegerField -> {
                if (type.const != null) {
                    companionBuilder.handleConst(
                        name = name,
                        lexiconType = type
                    )
                    return@forEach
                }
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )

                initCodeBlockBuilder.handleRequirements(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )
            }
            is StringField -> {
                if (type.const != null) {
                    companionBuilder.handleConst(
                        name = name,
                        lexiconType = type
                    )
                    return@forEach
                }
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )

                type.knownValues?.forEach { knownValue ->
                    val ref = LexiconRef(knownValue)
                    if (ref.isReference) {
                        val lexiconRef = environment.loadReference(context.lexicon, ref)
                        require(lexiconRef is TokenField)
                        companionBuilder.addProperty(
                            PropertySpec.builder(
                                ref.objectRef.uppercase(),
                                String::class,
                                KModifier.CONST, KModifier.PUBLIC
                            ).initializer("%S", ref.objectRef)
                                .handleDescription(lexiconRef)
                                .build()
                        )
                    } else {
                        companionBuilder.addProperty(
                            PropertySpec.builder(
                                knownValue.uppercase(),
                                String::class,
                                KModifier.PUBLIC, KModifier.CONST
                            ).initializer("%S", knownValue).build()
                        )
                    }
                }

                initCodeBlockBuilder.handleRequirements(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )
            }
            is BytesField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )

                initCodeBlockBuilder.handleRequirements(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )
            }
            is CidLinkField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )
            }
            is BlobField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )

                initCodeBlockBuilder.handleRequirements(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )
            }
            is ArrayField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    context = context,
                    environment = environment
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable,
                    context = context,
                    environment = environment
                )

                initCodeBlockBuilder.handleRequirements(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )
            }
            is ObjectField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    context = context
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable,
                    context = context
                )

                generateDataClass(type, context, environment)
            }
            is UnknownField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable
                )
            }
            is UnionField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    context = context
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable,
                    context = context
                )
                generateClass(type, context, environment)
            }
            is RefField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    context = context,
                    environment = environment
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable,
                    context = context,
                    environment = environment
                )
                type.handleRefGeneration(context, environment)
            }
            is TokenField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable,
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable,
                )
            }
            is RecordField -> {
                constructorBuilder.handleParam(
                    name = name,
                    lexiconType = type,
                    isRequired = isRequired,
                    isNullable = isNullable,
                    context = context
                )

                typeSpecBuilder.handleProperty(
                    name = name,
                    lexiconType = type,
                    isNullable = isNullable,
                    context = context
                )
            }
            else -> throw IllegalArgumentException("Unknown type to parse $type")
        }
    }

    if (constructorBuilder.parameters.isNotEmpty()) {
        typeSpecBuilder.primaryConstructor(constructorBuilder.build())
    }

    if (initCodeBlockBuilder.isNotEmpty()) {
        typeSpecBuilder.addInitializerBlock(initCodeBlockBuilder.build())
    }

    if (companionBuilder.typeSpecs.isNotEmpty() || companionBuilder.propertySpecs.isNotEmpty()) {
        typeSpecBuilder.addType(companionBuilder.build())
    }

    if (typeSpecBuilder.propertySpecs.isNotEmpty()) {
        typeSpecBuilder.addModifiers(KModifier.DATA)
    }

    dataFileBuilder.addType(typeSpecBuilder.build())

    val fileSpec = dataFileBuilder.build()

    environment.generateFile(fileSpec)
}

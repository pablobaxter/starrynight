package com.frybits.gradle.atproto.generator

import com.frybits.gradle.atproto.lexicon.Lexicon
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized

internal fun Project.generateLexicon(schema: Lexicon) {
    val packageName = schema.id

    val main = schema.defs["main"]

    if (main != null) {

    }
}

internal fun generateLexiconDefinitions(schema: Lexicon) {
    schema.defs.forEach { (name, def) ->
        val fileBuilder = FileSpec.builder(schema.id, name)
        if (schema.description != null) {
            fileBuilder.addFileComment("""
                GENERATED FILE. DO NOT MODIFY!
                Schema description: ${schema.description}
            """.trimIndent())
        } else {
            fileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")
        }

        when (def) {
            is BooleanField -> {
                val parameter = ParameterSpec.builder("prop", Boolean::class)

                if (def.default != null) {
                    parameter.defaultValue(def.default.toString())
                }

                val typeSpecBuilder = TypeSpec.classBuilder(name.capitalized())
                    .addAnnotation(JvmInline::class)
                    .addModifiers(KModifier.VALUE)
                    .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter(parameter.build())
                        .build())
                    .addProperty(
                        PropertySpec.builder("prop", Boolean::class)
                            .initializer("prop")
                            .build()
                    )

                if (def.const != null) {
                    typeSpecBuilder.addInitializerBlock(
                        CodeBlock.builder()
                            .addStatement("require(prop == %L) { %P }", def.const, $$"Expected $${def.const} but got $prop")
                            .build()
                    )
                }

                if (def.description != null) {
                    typeSpecBuilder.addKdoc(def.description)
                }
                println(fileBuilder.addType(typeSpecBuilder.build()).build())
            }
            else -> {}
        }
    }
}

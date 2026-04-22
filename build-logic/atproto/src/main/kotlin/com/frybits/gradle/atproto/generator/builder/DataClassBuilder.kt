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
import com.frybits.gradle.atproto.generator.builder.utils.handleRequirements
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.Serializable

internal fun generateClass(lexiconType: ObjectField, context: LexiconContext, environment: LexiconEnvironment, classNameSuffix: String = "") {
    val className = ClassName(context.authority, "${context.name}$classNameSuffix")
    val fileBuilder = createFileBuilder(className)

    val typeSpecBuilder = TypeSpec.classBuilder(className)
        .addAnnotation(Serializable::class)
        .addModifiers(KModifier.PUBLIC)

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
            }
        }
    }
}
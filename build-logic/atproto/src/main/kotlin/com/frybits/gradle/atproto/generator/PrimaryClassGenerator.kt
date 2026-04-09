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
import com.frybits.gradle.atproto.lexicon.categories.ErrorBodyField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.MessageField
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.ParamsField
import com.frybits.gradle.atproto.lexicon.categories.PermissionSetField
import com.frybits.gradle.atproto.lexicon.categories.ProcedureField
import com.frybits.gradle.atproto.lexicon.categories.QueryField
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.lexicon.categories.RepoPermissionField
import com.frybits.gradle.atproto.lexicon.categories.RpcPermissionField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.SubscriptionField
import com.frybits.gradle.atproto.lexicon.categories.TokenField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.Serializable

internal fun RecordField.generateClass(
    className: ClassName,
    toGenerateCollector: MutableSet<ClassName>
): FileSpec {
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
    val required = record.required.orEmpty().toSet()

    record.properties.forEach { (name, type) ->
        val isRequired = name in required
        when (type) {
            is BlobField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    initCodeBlockBuilder = initCodeBlock,
                    isRequired = isRequired
                )
            }
            is BooleanField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    companionBuilder = companion,
                    isRequired = isRequired
                )
            }
            is BytesField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    initCodeBlockBuilder = initCodeBlock,
                    isRequired = isRequired
                )
            }
            is IntegerField -> {
                type.generateField(
                    name = name,
                    typeSpecBuilder = typeSpecBuilder,
                    constructorBuilder = constructor,
                    initCodeBlockBuilder = initCodeBlock,
                    companionBuilder = companion,
                    isRequired = isRequired
                )
            }
            is StringField -> TODO()
            is CidLinkField -> TODO()
            is ArrayField -> TODO()
            is ObjectField -> TODO()
            is ErrorBodyField -> TODO()
            is MessageField -> TODO()
            is RefField -> TODO()
            is TokenField -> TODO()
            is UnionField -> TODO()
            is UnknownField -> TODO()
            is PermissionSetField -> TODO()
            is RecordField -> TODO()
            is ProcedureField -> TODO()
            is QueryField -> TODO()
            is SubscriptionField -> TODO()
            is ParamsField -> TODO()
            is RepoPermissionField -> TODO()
            is RpcPermissionField -> TODO()
            else -> TODO()
        }
    }

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

    return fileBuilder.addType(typeSpecBuilder.build()).build()
}
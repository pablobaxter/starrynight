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
import com.frybits.gradle.atproto.lexicon.categories.BodyField
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
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language

internal fun RecordField.generateClass(
    className: ClassName,
    toGenerateCollector: MutableSet<ClassName>
): FileSpec {
    val fileBuilder = FileSpec.builder(className)

    fileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")

    val typeSpecBuilder = TypeSpec.classBuilder(className)
        .addAnnotation(Serializable::class)
        .addModifiers(KModifier.PUBLIC)
        .addModifiers(KModifier.DATA)

    if (description != null) {
        typeSpecBuilder.addKdoc(description)
    }

    val constructor = FunSpec.constructorBuilder()

    require(record is ObjectField)
    val required = record.required.orEmpty().toSet()

    val initCodeBlock = CodeBlock.builder()

    record.properties.forEach { (name, type) ->
        when (type) {
            is BlobField -> {
                val blobClassName = ClassName(packageName = "com.frybits.starrynight.atproto.models.blob", "Blob")
                val standardBlobClassName = ClassName(packageName = "com.frybits.starrynight.atproto.models.blob", "StandardBlob")

                val property = PropertySpec.builder(name, blobClassName)
                    .initializer(name)

                val parameter = ParameterSpec.builder(name,blobClassName)
                if (type.description != null) {
                    parameter.addKdoc(type.description)
                    property.addKdoc(type.description)
                }

                initCodeBlock.addStatement("// Begin $name requirements")
                if (!type.accept.isNullOrEmpty()) {
                    initCodeBlock.addStatement("val accept = setOf(%L)", type.accept.joinToString(", ") { "\"$it\"" })
                    initCodeBlock.addStatement("require(%L in accept) { %P }", "$name.mimeType", $$"$$name does not allow mimeType ${$$name.mimeType}")
                }
                if (type.maxSize != null) {
                    initCodeBlock.beginControlFlow("if (%L is %T)", name, standardBlobClassName)
                        .addStatement("require(%L <= %L) { %P }", "$name.size", type.maxSize, $$"$$name cannot be bigger than $${type.maxSize} bytes. Current size: ${$$name.size} bytes")
                        .endControlFlow()
                }

                initCodeBlock.addStatement("// End $name requirements")

                constructor.addParameter(parameter.build())
                typeSpecBuilder.addProperty(property.build())
            }
            is BooleanField -> TODO()
            is BytesField -> TODO()
            is CidLinkField -> TODO()
            is IntegerField -> TODO()
            is StringField -> TODO()
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

    typeSpecBuilder.primaryConstructor(constructor.build())
    typeSpecBuilder.addInitializerBlock(initCodeBlock.build())

    return fileBuilder.addType(typeSpecBuilder.build()).build()
}
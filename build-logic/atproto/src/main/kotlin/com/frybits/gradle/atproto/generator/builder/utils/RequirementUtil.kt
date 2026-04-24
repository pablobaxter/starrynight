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

package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.lexicon.categories.ArrayField
import com.frybits.gradle.atproto.lexicon.categories.BlobField
import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.StringFormat
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.withIndent
import java.net.URI
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal fun CodeBlock.Builder.handleRequirements(name: String, lexiconType: LexiconType, isNullable: Boolean) {
    val outerCodeBlock = CodeBlock.builder()
    val innerCodeBlock = CodeBlock.builder()

    if (isNotEmpty()) {
        outerCodeBlock.addStatement("")
    }

    outerCodeBlock.addStatement("// Begin $name requirements")
    if (isNullable) {
        outerCodeBlock.beginControlFlow("if (%L != null)", name)
    }

    when (lexiconType) {
        is IntegerField -> handleRequirements(name, lexiconType, innerCodeBlock)
        is StringField -> handleRequirements(name, lexiconType, innerCodeBlock)
        is BytesField -> handleRequirements(name, lexiconType, innerCodeBlock)
        is BlobField -> handleRequirements(name, lexiconType, innerCodeBlock)
        is ArrayField -> handleRequirements(name, lexiconType, innerCodeBlock)
        else -> Unit
    }

    if (innerCodeBlock.isNotEmpty()) {
        outerCodeBlock.add(innerCodeBlock.build())
    }
    if (isNullable) {
        outerCodeBlock.endControlFlow()
    }
    outerCodeBlock.addStatement("// End $name requirements")

    if (innerCodeBlock.isNotEmpty()) {
        add(outerCodeBlock.build())
    }
}

private fun handleRequirements(
    name: String,
    lexiconType: IntegerField,
    codeBlockBuilder: CodeBlock.Builder
) {
    val minimum = lexiconType.minimum
    if (minimum != null) {
        codeBlockBuilder.addStatement("require(%L >= %L) { %P }", name, minimum, $$"Expected $$name to be greater than or equal to $$minimum. Currently $$$name")
    }
    val maximum = lexiconType.maximum
    if (maximum != null) {
        codeBlockBuilder.addStatement("require(%L <= %L) { %P }", name, maximum, $$"Expected $$name to be less than or equal to $$maximum. Currently $$$name")
    }
    val enum = lexiconType.enum
    if (!enum.isNullOrEmpty()) {
        codeBlockBuilder.addStatement("val enums = setOf(%L)", enum.joinToString(", "))
        codeBlockBuilder.addStatement("require(%L in enums) { %P }", name, $$"$$name must be one of $enums")
    }
}

@OptIn(ExperimentalTime::class)
private fun handleRequirements(
    name: String,
    lexiconType: StringField,
    codeBlockBuilder: CodeBlock.Builder
) {
    val minLength = lexiconType.minLength
    if (minLength != null) {
        codeBlockBuilder.addStatement("require(%L.length >= %L) { %P }", name, minLength, $$"Expected $$name to be greater than or equal to $$minLength. Current size ${$$name.length}")
    }
    val maxLength = lexiconType.maxLength
    if (maxLength != null) {
        codeBlockBuilder.addStatement("require(%L.length <= %L) { %P }", name, maxLength, $$"Expected $$name to be less than or equal to $$maxLength. Current size ${$$name.length}")
    }
    val maxGraphemes = lexiconType.maxGraphemes
    val minGraphemes = lexiconType.minGraphemes
    if (maxGraphemes != null || minGraphemes != null) {
        codeBlockBuilder.addStatement("val graphemes = \"\\\\X\".toRegex().findAll(%L).count()", name)
        if (maxGraphemes != null) {
            codeBlockBuilder.addStatement("require(graphemes <= %L) { %P }", maxGraphemes, $$"Expected graphemes length to be less than or equal to $$maxGraphemes")
        }
        if (minGraphemes != null) {
            codeBlockBuilder.addStatement("require(graphemes >= %L) { %P }", minGraphemes, $$"Expected graphemes length to be greater than or equal to $$minGraphemes")
        }
    }
    val enum = lexiconType.enum
    if (enum != null) {
        codeBlockBuilder.addStatement("val enumList = setOf(")
        codeBlockBuilder.withIndent {
            when (lexiconType.format) {
                StringFormat.DATETIME -> enum.forEach {
                    addStatement("%T.parse(%S),", Instant::class, it)
                }
                StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> enum.forEach {
                    addStatement("%T.create(%S),", URI::class, it)
                }
                StringFormat.AT_IDENTIFIER -> {
                    enum.forEach {
                        if (it.startsWith("did:")) {
                            addStatement("%T(%S),", TypeNames.Did, it)
                        } else {
                            addStatement("%T(%S),", TypeNames.Handle, it)
                        }
                    }
                }
                null -> enum.forEach { addStatement("%S,", it) }
                else -> enum.forEach { addStatement("%T(%S),", lexiconType.getTypeName(), it) }
            }
        }
        codeBlockBuilder.addStatement(")")
        codeBlockBuilder.addStatement("require(%L in enumList) { %P }", name, $$"Expected $$$name to be in $enumList")
    }
}

private fun handleRequirements(
    name: String,
    lexiconType: BytesField,
    codeBlockBuilder: CodeBlock.Builder
) {
    val minLength = lexiconType.minLength
    if (minLength != null) {
        codeBlockBuilder.addStatement("require(%L.size >= %L) { %P }", name, minLength, $$"Expected $$name to be greater than or equal to $$minLength. Current size ${$$name.size}")
    }
    val maxLength = lexiconType.maxLength
    if (maxLength != null) {
        codeBlockBuilder.addStatement("require(%L.size <= %L) { %P }", name, maxLength, $$"Expected $$name to be less than or equal to $$maxLength. Current size ${$$name.size}")
    }
}

private fun handleRequirements(
    name: String,
    lexiconType: BlobField,
    codeBlockBuilder: CodeBlock.Builder
) {
    val accept = lexiconType.accept
    if (!accept.isNullOrEmpty()) {
        codeBlockBuilder.addStatement("val accept = setOf(%L)", accept.joinToString(", ") { "\"$it\"" })
        codeBlockBuilder.addStatement("require(%L.mimeType in accept) { %P }", name, $$"$$name does not allow mimeType $$$name")
    }
    val maxSize = lexiconType.maxSize
    if (maxSize != null) {
        codeBlockBuilder.beginControlFlow("if (%L is %T)", name, TypeNames.StandardBlob)
            .addStatement("require(%L.size <= %L) { %P }", name, maxSize, $$"$$name cannot be bigger than $$maxSize bytes. Current size: ${$$name.size} bytes")
            .endControlFlow()
    }
}

private fun handleRequirements(
    name: String,
    lexiconType: ArrayField,
    codeBlockBuilder: CodeBlock.Builder
) {
    val minLength = lexiconType.minLength
    if (minLength != null) {
        codeBlockBuilder.addStatement("require(%L.size >= %L) { %P }", name, minLength, $$"Expected $$name size to be greater than or equal to $$minLength. Currently ${$$name.size}")
    }
    val maxLength = lexiconType.maxLength
    if (maxLength != null) {
        codeBlockBuilder.addStatement("require(%L.size <= %L) { %P }", name, maxLength, $$"Expected $$name size to be less than or equal to $$maxLength. Currently ${$$name.size}")
    }
}

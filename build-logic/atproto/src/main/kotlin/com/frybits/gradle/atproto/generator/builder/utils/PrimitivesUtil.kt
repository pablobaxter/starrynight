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

import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.squareup.kotlinpoet.CodeBlock

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

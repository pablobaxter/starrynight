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
import com.frybits.gradle.atproto.generator.builder.utils.handleDescription
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.categories.ProcedureField
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

internal fun generateClass(lexiconType: ProcedureField, context: LexiconContext, environment: LexiconEnvironment) {
    val className = ClassName(context.authority, "${context.name}Api")
    val fileBuilder = createFileBuilder(className)

    val typeSpecBuilder = TypeSpec.interfaceBuilder(className)
        .addModifiers(KModifier.PUBLIC)
        .handleDescription(lexiconType)

    val procedureFunSpecBuilder = FunSpec.builder(context.name.lowercase())
        .addModifiers(KModifier.PUBLIC, KModifier.SUSPEND, KModifier.ABSTRACT)


}

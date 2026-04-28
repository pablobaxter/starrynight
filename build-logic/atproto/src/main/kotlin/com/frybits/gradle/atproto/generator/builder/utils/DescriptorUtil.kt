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

import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal fun FileSpec.Builder.handleDescription(lexiconType: LexiconType): FileSpec.Builder {
    val description = lexiconType.description.takeIf { !it.isNullOrBlank() } ?: return this
    return addFileComment(description)
}

internal fun TypeSpec.Builder.handleDescription(lexiconType: LexiconType): TypeSpec.Builder {
    val description = lexiconType.description.takeIf { !it.isNullOrBlank() } ?: return this
    return addKdoc(description)
}

internal fun PropertySpec.Builder.handleDescription(lexiconType: LexiconType): PropertySpec.Builder {
    val description = lexiconType.description.takeIf { !it.isNullOrBlank() } ?: return this
    return addKdoc(description)
}

internal fun ParameterSpec.Builder.handleDescription(lexiconType: LexiconType): ParameterSpec.Builder {
    val description = lexiconType.description.takeIf { !it.isNullOrBlank() } ?: return this
    return addKdoc(description)
}

internal fun FunSpec.Builder.handleDescription(lexiconType: LexiconType): FunSpec.Builder {
    val description = lexiconType.description.takeIf { !it.isNullOrBlank() } ?: return this
    return addKdoc(description)
}

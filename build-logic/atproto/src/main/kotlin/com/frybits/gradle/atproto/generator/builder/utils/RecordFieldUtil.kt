/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 pablo
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

import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.utils.titleCaseFirstChar
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal fun FunSpec.Builder.handleParam(name: String, lexiconType: RecordField, isRequired: Boolean, isNullable: Boolean, context: LexiconContext) {
    val typeName = ClassName(context.authority, context.authority.split('.').last().titleCaseFirstChar()).copy(nullable = isNullable)

    val parameter = ParameterSpec.builder(name, typeName)
    parameter.handleDescription(lexiconType)

    if (!isRequired) {
        if (isNullable) {
            parameter.defaultValue("%L", null)
        }
    }

    addParameter(parameter.build())
}

internal fun TypeSpec.Builder.handleProperty(name: String, lexiconType: RecordField, isNullable: Boolean, context: LexiconContext) {
    val typeName = ClassName(context.authority, context.authority.split('.').last().titleCaseFirstChar()).copy(nullable = isNullable)

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    property.handleDescription(lexiconType)

    addProperty(property.build())
}

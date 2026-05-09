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
import com.frybits.gradle.atproto.utils.camelToSnakeCase
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

internal fun TypeSpec.Builder.handleConst(name: String, lexiconType: IntegerField) {
    val const = lexiconType.const ?: return
    val property = PropertySpec.builder(name.camelToSnakeCase().uppercase(), Int::class)
        .addModifiers(KModifier.INTERNAL, KModifier.CONST)
        .initializer("%L", const)

    property.handleDescription(lexiconType)

    addProperty(property.build())
}

internal fun FunSpec.Builder.handleParam(name: String, lexiconType: IntegerField, isRequired: Boolean, isNullable: Boolean) {
    val typeName = Int::class.asTypeName().copy(nullable = isNullable)
    val parameter = ParameterSpec.builder(name, typeName)
    parameter.handleDescription(lexiconType)

    val default = lexiconType.default
    if (default != null) {
        parameter.defaultValue("%L", default)
    } else if (!isRequired) {
        if (isNullable) {
            parameter.defaultValue("%L", null)
        } else {
            parameter.defaultValue("%L", 0)
        }
    }

    addParameter(parameter.build())
}

internal fun TypeSpec.Builder.handleProperty(name: String, lexiconType: IntegerField, isNullable: Boolean) {
    val typeName = Int::class.asTypeName().copy(nullable = isNullable)
    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    property.handleDescription(lexiconType)

    addProperty(property.build())
}

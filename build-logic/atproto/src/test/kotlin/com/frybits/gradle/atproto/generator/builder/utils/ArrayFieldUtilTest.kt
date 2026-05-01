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

import com.frybits.gradle.atproto.fakes.FakeDirectory
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.Lexicon
import com.frybits.gradle.atproto.lexicon.categories.ArrayField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import kotlin.test.Test
import kotlin.test.assertEquals

class ArrayFieldUtilTest {

    @Test
    fun `handle param test`() {
        val funspecBuilder = FunSpec.builder("test")

        val arrayLexiconField = ArrayField(
            items = StringField()
        )

        funspecBuilder.handleParam(
            name = "foobar",
            lexiconType = arrayLexiconField,
            isRequired = false,
            isNullable = false,
            environment = LexiconEnvironment(emptyList(), FakeDirectory()),
            context = LexiconContext("test", Lexicon(lexicon = 1, id = "", defs = emptyMap()))
        )

        val parameters = funspecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(List::class.asTypeName().parameterizedBy(String::class.asTypeName()), param.type)
    }
}

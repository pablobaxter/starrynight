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
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArrayFieldUtilTest {

    @Test
    fun `handle param test`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = ArrayField(
            items = StringField()
        )

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = false,
            environment = LexiconEnvironment(emptyList(), FakeDirectory()),
            context = LexiconContext("test", Lexicon(lexicon = 1, id = "", defs = emptyMap()))
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(List::class.asTypeName().parameterizedBy(String::class.asTypeName()), param.type)
        assertEquals("emptyList()", param.defaultValue.toString())
    }

    @Test
    fun `handle param with required value`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = ArrayField(
            items = StringField()
        )

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = true,
            isNullable = false,
            environment = LexiconEnvironment(emptyList(), FakeDirectory()),
            context = LexiconContext("test", Lexicon(lexicon = 1, id = "", defs = emptyMap()))
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(List::class.asTypeName().parameterizedBy(String::class.asTypeName()), param.type)
        assertNull(param.defaultValue)
    }

    @Test
    fun `handle param test with nullable`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = ArrayField(
            items = StringField()
        )

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = true,
            environment = LexiconEnvironment(emptyList(), FakeDirectory()),
            context = LexiconContext("test", Lexicon(lexicon = 1, id = "", defs = emptyMap()))
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(List::class.asTypeName().parameterizedBy(String::class.asTypeName()).copy(nullable = true), param.type)
        assertEquals("null", param.defaultValue.toString())
    }

    @Test
    fun `handle property test`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = ArrayField(
            items = StringField()
        )

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = false,
            environment = LexiconEnvironment(emptyList(), FakeDirectory()),
            context = LexiconContext("test", Lexicon(lexicon = 1, id = "", defs = emptyMap()))
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.name)
        assertEquals("foobar", property.initializer.toString())
        assertEquals(List::class.asTypeName().parameterizedBy(String::class.asTypeName()), property.type)
    }

    @Test
    fun `handle property test with nullable`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = ArrayField(
            items = StringField()
        )

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = true,
            environment = LexiconEnvironment(emptyList(), FakeDirectory()),
            context = LexiconContext("test", Lexicon(lexicon = 1, id = "", defs = emptyMap()))
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.initializer.toString())
        assertEquals(List::class.asTypeName().parameterizedBy(String::class.asTypeName()).copy(nullable = true), property.type)
    }
}

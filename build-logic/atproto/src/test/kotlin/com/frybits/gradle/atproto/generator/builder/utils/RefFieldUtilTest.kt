package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.fakes.FakeDirectory
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.Lexicon
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RefFieldUtilTest {

    @Test
    fun `handle param test`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = RefField(ref = "#blah")

        funSpecBuilder.handleParam(
            name = "baz",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = false,
            context = LexiconContext("Test", Lexicon(lexicon = 1, id = "com.foobar", defs = mapOf("blah" to ObjectField(properties = emptyMap())))),
            environment = LexiconEnvironment(emptyList(), FakeDirectory())
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(ClassName("com.foobar", "Blah"), param.type)
    }

    @Test
    fun `handle param with required value`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = RefField(ref = "#blah")

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = true,
            isNullable = false,
            context = LexiconContext("Test", Lexicon(lexicon = 1, id = "com.foobar", defs = mapOf("blah" to ObjectField(properties = emptyMap())))),
            environment = LexiconEnvironment(emptyList(), FakeDirectory())
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(ClassName("com.foobar", "Blah"), param.type)
        assertNull(param.defaultValue)
    }

    @Test
    fun `handle param test with nullable`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = RefField(ref = "#blah")

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = true,
            context = LexiconContext("Test", Lexicon(lexicon = 1, id = "com.foobar", defs = mapOf("blah" to ObjectField(properties = emptyMap())))),
            environment = LexiconEnvironment(emptyList(), FakeDirectory())
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(ClassName("com.foobar", "Blah").copy(nullable = true), param.type)
        assertEquals("null", param.defaultValue.toString())
    }

    @Test
    fun `handle property test`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = RefField(ref = "#blah")

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = false,
            context = LexiconContext("Test", Lexicon(lexicon = 1, id = "com.foobar", defs = mapOf("blah" to ObjectField(properties = emptyMap())))),
            environment = LexiconEnvironment(emptyList(), FakeDirectory())
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.name)
        assertEquals("foobar", property.initializer.toString())
        assertEquals(ClassName("com.foobar", "Blah"), property.type)
    }

    @Test
    fun `handle property test with nullable`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = RefField(ref = "#blah")

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = true,
            context = LexiconContext("Test", Lexicon(lexicon = 1, id = "com.foobar", defs = mapOf("blah" to ObjectField(properties = emptyMap())))),
            environment = LexiconEnvironment(emptyList(), FakeDirectory())
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.initializer.toString())
        assertEquals(ClassName("com.foobar", "Blah").copy(nullable = true), property.type)
    }
}
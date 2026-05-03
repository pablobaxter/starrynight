package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BooleanFieldUtilTest {

    @Test
    fun `handle param test`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BooleanField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = false,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(Boolean::class.asTypeName(), param.type)
        assertEquals("false", param.defaultValue.toString())
    }

    @Test
    fun `handle param with required value`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BooleanField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = true,
            isNullable = false,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(Boolean::class.asTypeName(), param.type)
        assertNull(param.defaultValue)
    }

    @Test
    fun `handle param test with nullable`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BooleanField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = true,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(Boolean::class.asTypeName().copy(nullable = true), param.type)
        assertEquals("null", param.defaultValue.toString())
    }

    @Test
    fun `handle property test`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = BooleanField()

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = false
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.name)
        assertEquals("foobar", property.initializer.toString())
        assertEquals(Boolean::class.asTypeName(), property.type)
    }

    @Test
    fun `handle property test with nullable`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = BooleanField()

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = true
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.initializer.toString())
        assertEquals(Boolean::class.asTypeName().copy(nullable = true), property.type)
    }

    @Test
    fun `handle const test`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = BooleanField(
            const = true
        )

        typeSpecBuilder.handleConst(
            name = "foobarTest",
            lexiconType = lexiconField
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("FOOBAR_TEST", property.name)
        assertContentEquals(listOf(KModifier.PUBLIC, KModifier.CONST), property.modifiers)
        assertEquals("true", property.initializer.toString())
    }
}

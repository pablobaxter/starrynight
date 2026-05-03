package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BytesFieldUtilTest {

    @Test
    fun `handle param test`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BytesField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = false,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(ByteArray::class.asTypeName(), param.type)
        assertEquals("byteArrayOf()", param.defaultValue.toString())
    }

    @Test
    fun `handle param with required value`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BytesField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = true,
            isNullable = false,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(ByteArray::class.asTypeName(), param.type)
        assertNull(param.defaultValue)
    }

    @Test
    fun `handle param test with nullable`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BytesField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = true,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(ByteArray::class.asTypeName().copy(nullable = true), param.type)
        assertEquals("null", param.defaultValue.toString())
    }

    @Test
    fun `handle property test`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = BytesField()

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
        assertEquals(ByteArray::class.asTypeName(), property.type)
    }

    @Test
    fun `handle property test with nullable`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = BytesField()

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = true
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.initializer.toString())
        assertEquals(ByteArray::class.asTypeName().copy(nullable = true), property.type)
    }
}

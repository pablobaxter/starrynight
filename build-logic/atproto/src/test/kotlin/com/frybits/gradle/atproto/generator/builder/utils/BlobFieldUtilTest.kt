package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.lexicon.categories.BlobField
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BlobFieldUtilTest {

    @Test
    fun `handle param test`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BlobField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = false,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(TypeNames.Blob, param.type)
        assertEquals(TypeNames.EmptyBlob.toString(), param.defaultValue.toString())
    }

    @Test
    fun `handle param with required value`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BlobField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = true,
            isNullable = false,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(TypeNames.Blob, param.type)
        assertNull(param.defaultValue)
    }

    @Test
    fun `handle param test with nullable`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = BlobField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = true,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(TypeNames.Blob.copy(nullable = true), param.type)
        assertEquals("null", param.defaultValue.toString())
    }

    @Test
    fun `handle property test`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = BlobField()

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
        assertEquals(TypeNames.Blob, property.type)
    }

    @Test
    fun `handle property test with nullable`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = BlobField()

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = true
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.initializer.toString())
        assertEquals(TypeNames.Blob.copy(nullable = true), property.type)
    }
}

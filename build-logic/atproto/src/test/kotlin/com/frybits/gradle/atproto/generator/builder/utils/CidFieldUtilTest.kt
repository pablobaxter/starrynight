package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.lexicon.categories.CidLinkField
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CidFieldUtilTest {

    @Test
    fun `handle param test`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = CidLinkField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = false,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(URI::class.asTypeName(), param.type)
        assertEquals("${URI::class.asTypeName()}.create(\"\")", param.defaultValue.toString())
    }

    @Test
    fun `handle param with required value`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = CidLinkField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = true,
            isNullable = false,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(URI::class.asTypeName(), param.type)
        assertNull(param.defaultValue)
    }

    @Test
    fun `handle param test with nullable`() {
        val funSpecBuilder = FunSpec.builder("test")

        val lexiconField = CidLinkField()

        funSpecBuilder.handleParam(
            name = "foobar",
            lexiconType = lexiconField,
            isRequired = false,
            isNullable = true,
        )

        val parameters = funSpecBuilder.parameters
        assertEquals(1, parameters.size)
        val param = parameters.first()
        assertEquals(URI::class.asTypeName().copy(nullable = true), param.type)
        assertEquals("null", param.defaultValue.toString())
    }

    @Test
    fun `handle property test`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = CidLinkField()

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
        assertEquals(URI::class.asTypeName(), property.type)
    }

    @Test
    fun `handle property test with nullable`() {
        val typeSpecBuilder = TypeSpec.classBuilder("test")

        val lexiconField = CidLinkField()

        typeSpecBuilder.handleProperty(
            name = "foobar",
            lexiconType = lexiconField,
            isNullable = true
        )

        val properties = typeSpecBuilder.propertySpecs
        assertEquals(1, properties.size)
        val property = properties.first()
        assertEquals("foobar", property.initializer.toString())
        assertEquals(URI::class.asTypeName().copy(nullable = true), property.type)
    }
}

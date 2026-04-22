package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.utils.lexiconJson
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ConcreteTest {

    @Test
    fun `boolean field deserialization`() {
        @Language("JSON")
        val booleanFieldJson = """
            {
              "type": "boolean",
              "description": "Some description",
              "default": true,
              "const": false
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(booleanFieldJson)

        val expected = BooleanField(
            description = "Some description",
            default = true,
            const = false
        )

        assertIs<BooleanField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `boolean field deserialization minimal`() {
        @Language("JSON")
        val booleanFieldJson = """
            {"type": "boolean"}
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(booleanFieldJson)

        val expected = BooleanField()

        assertIs<BooleanField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `integer field deserialization`() {
        @Language("JSON")
        val integerFieldJson = """
            {
              "type": "integer",
              "description": "Some description",
              "minimum": 1,
              "maximum": 10,
              "enum": [1, 2, 3],
              "default": 7,
              "const": 2
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(integerFieldJson)

        val expected = IntegerField(
            description = "Some description",
            minimum = 1,
            maximum = 10,
            enum = listOf(1, 2, 3),
            default = 7,
            const = 2
        )

        assertIs<IntegerField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `integer field deserialization minimal`() {
        @Language("JSON")
        val integerFieldJson = """
            {"type": "integer"}
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(integerFieldJson)

        val expected = IntegerField()

        assertIs<IntegerField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `string field deserialization`() {
        @Language("JSON")
        val stringFieldJson = """
            {
              "type": "string",
              "description": "Some description",
              "format": "at-identifier",
              "maxLength": 10,
              "minLength": 3,
              "maxGraphemes": 7,
              "minGraphemes": 2,
              "knownValues": ["hi", "world"],
              "enum": ["1", "2", "3"],
              "default": "word",
              "const": "const"
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(stringFieldJson)

        val expected = StringField(
            description = "Some description",
            format = StringFormat.AT_IDENTIFIER,
            maxLength = 10,
            minLength = 3,
            maxGraphemes = 7,
            minGraphemes = 2,
            knownValues = listOf("hi", "world"),
            enum = listOf("1", "2", "3"),
            default = "word",
            const = "const"
        )

        assertIs<StringField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `string field deserialization minimal`() {
        @Language("JSON")
        val stringFieldJson = """
            {"type": "string"}
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(stringFieldJson)

        val expected = StringField()

        assertIs<StringField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `bytes field deserialization`() {
        @Language("JSON")
        val byteFieldJson = """
            {
              "type": "bytes",
              "description": "Some description",
              "minLength": 3,
              "maxLength": 10
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(byteFieldJson)

        val expected = BytesField(
            description = "Some description",
            maxLength = 10,
            minLength = 3,
        )

        assertIs<BytesField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `bytes field deserialization minimal`() {
        @Language("JSON")
        val byteFieldJson = """
            {"type": "bytes"}
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(byteFieldJson)

        val expected = BytesField()

        assertIs<BytesField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `cid-link field deserialization`() {
        @Language("JSON")
        val cidLinkFieldJson = """
            {
              "type": "cid-link",
              "description": "Some description"
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(cidLinkFieldJson)

        val expected = CidLinkField(
            description = "Some description",
        )

        assertIs<CidLinkField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `cid-link field deserialization minimal`() {
        @Language("JSON")
        val cidLinkFieldJson = """
            {"type": "cid-link"}
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(cidLinkFieldJson)

        val expected = CidLinkField()

        assertIs<CidLinkField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `blob field deserialization`() {
        @Language("JSON")
        val blobFieldJson = """
            {
              "type": "blob",
              "description": "Some description",
              "accept": ["blah"],
              "maxSize": 2
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(blobFieldJson)

        val expected = BlobField(
            description = "Some description",
            maxSize = 2,
            accept = listOf("blah")
        )

        assertIs<BlobField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `blob field deserialization minimal`() {
        @Language("JSON")
        val blobFieldJson = """
            {"type": "blob"}
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(blobFieldJson)

        val expected = BlobField()

        assertIs<BlobField>(result)
        assertIs<ConcreteField>(result)
        assertEquals(expected, result)
    }
}

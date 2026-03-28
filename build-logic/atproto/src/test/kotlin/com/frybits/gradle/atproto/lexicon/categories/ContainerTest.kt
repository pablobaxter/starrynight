package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.lexicon.lexiconJson
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ContainerTest {

    @Test
    fun `array field deserialization`() {
        @Language("JSON")
        val arrayFieldJson = """
            {
              "type": "array",
              "description": "Some description",
              "items": { "type": "cid-link" },
              "minLength": 2,
              "maxLength": 3
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(arrayFieldJson)

        val expected = ArrayField(
            description = "Some description",
            items = CidLinkField(),
            minLength = 2,
            maxLength = 3,
        )

        assertIs<ArrayField>(result)
        assertIs<ContainerField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `array field deserialization minimal`() {
        @Language("JSON")
        val arrayFieldJson = """
            {
              "type": "array",
              "items": { "type": "cid-link" }
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(arrayFieldJson)

        val expected = ArrayField(
            items = CidLinkField()
        )

        assertIs<ArrayField>(result)
        assertIs<ContainerField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `object field deserialization`() {
        @Language("JSON")
        val objectFieldJson = """
            {
              "type": "object",
              "description": "Some description",
              "properties": {
                "foobar": {
                  "type": "cid-link"
                } 
              },
              "required": ["foobar"],
              "nullable": []
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(objectFieldJson)

        val expected = ObjectField(
            description = "Some description",
            properties = mapOf("foobar" to CidLinkField()),
            required = listOf("foobar"),
            nullable = emptyList()
        )

        assertIs<ObjectField>(result)
        assertIs<ContainerField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `object field deserialization minimal`() {
        @Language("JSON")
        val objectFieldJson = """
            {
              "type": "object",
              "properties": {}
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(objectFieldJson)

        val expected = ObjectField(
            properties = emptyMap()
        )

        assertIs<ObjectField>(result)
        assertIs<ContainerField>(result)
        assertEquals(expected, result)
    }
}

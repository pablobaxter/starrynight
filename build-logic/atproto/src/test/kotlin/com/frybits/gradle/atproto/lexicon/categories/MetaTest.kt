package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.lexicon.lexiconJson
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MetaTest {

    @Test
    fun `token field deserialization`() {
        @Language("JSON")
        val tokenFieldJson = """
            {
              "type": "token",
              "description": "Some description"
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(tokenFieldJson)

        val expected = TokenField(
            description = "Some description",
        )

        assertIs<TokenField>(result)
        assertIs<MetaField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `ref field deserialization`() {
        @Language("JSON")
        val refFieldJson = """
            {
              "type": "ref",
              "description": "Some description",
              "ref": {
                "type": "string"
              }
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(refFieldJson)

        val expected = RefField(
            description = "Some description",
            ref = StringField()
        )

        assertIs<RefField>(result)
        assertIs<MetaField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `union field deserialization`() {
        @Language("JSON")
        val unionFieldJson = """
            {
              "type": "union",
              "description": "Some description",
              "refs": [{ "type": "string" }],
              "closed": true
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(unionFieldJson)

        val expected = UnionField(
            description = "Some description",
            refs = listOf(StringField()),
            closed = true
        )

        assertIs<UnionField>(result)
        assertIs<MetaField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `unknown field deserialization`() {
        @Language("JSON")
        val unknownFieldJson = """
            {
              "type": "unknown",
              "description": "Some description"
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(unknownFieldJson)

        val expected = UnknownField(
            description = "Some description",
        )

        assertIs<UnknownField>(result)
        assertIs<MetaField>(result)
        assertEquals(expected, result)
    }
}

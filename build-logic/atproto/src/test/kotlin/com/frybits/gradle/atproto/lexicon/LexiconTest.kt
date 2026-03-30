package com.frybits.gradle.atproto.lexicon

import com.frybits.gradle.atproto.generator.generateLexiconDefinitions
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals

class LexiconTest {

    @Test
    fun `lexicon base`() {
        @Language("JSON")
        val json = """
            {
              "lexicon": 1,
              "id": "app.bsky.feed.foobar",
              "description": "Some description",
              "defs": {
                "main": {
                  "type": "boolean"
                }
              }
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<Lexicon>(json)

        val expected = Lexicon(
            lexicon = 1,
            id = "app.bsky.feed.foobar",
            description = "Some description",
            defs = mapOf(
                "main" to BooleanField(
                    description = "foobar blah blah",
                    default = false,
                    const = true
                )
            )
        )

        generateLexiconDefinitions(expected)

        assertEquals(expected, result)
    }

    @Test
    fun `lexicon base minimal`() {
        @Language("JSON")
        val json = """
            {
              "lexicon": 1,
              "id": "app.bsky.feed.foobar",
              "defs": {}
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<Lexicon>(json)

        val expected = Lexicon(
            lexicon = 1,
            id = "app.bsky.feed.foobar",
            defs = emptyMap()
        )

        assertEquals(expected, result)
    }
}
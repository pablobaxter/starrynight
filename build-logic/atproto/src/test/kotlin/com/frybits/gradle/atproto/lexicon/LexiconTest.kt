package com.frybits.gradle.atproto.lexicon

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
                  "type": "string"
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
                "main" to StringField()
            )
        )

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
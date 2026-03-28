package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.lexicon.lexiconJson
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrimaryTest {

    @Test
    fun `record field deserialization`() {
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
package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.lexicon.lexiconJson
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertIsNot

class SubTypeTest {

    @Test
    fun `params field deserialization`() {
        @Language("JSON")
        val paramsFieldJson = """
            {
              "type": "params",
              "description": "Some description",
              "required": ["did"],
              "properties": {
                "did": {
                  "type": "string"
                }              
              }
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(paramsFieldJson)

        val expected = ParamsField(
            description = "Some description",
            required = listOf("did"),
            properties = mapOf("did" to StringField())
        )

        assertIs<ParamsField>(result)
        assertIs<SubTypeField>(result)
        assertIsNot<PermissionField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `repo permission field deserialization`() {
        @Language("JSON")
        val repoPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "repo",
              "description": "Some description",
              "collection": ["*"],
              "action": ["delete"]
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(repoPermissionFieldJson)

        val expected = RepoPermissionField(
            description = "Some description",
            resource = "repo",
            collection = listOf("*"),
            action = setOf(Action.DELETE)
        )

        assertIs<RepoPermissionField>(result)
        assertIs<SubTypeField>(result)
        assertIs<PermissionField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `rpc permission field deserialization`() {
        @Language("JSON")
        val rpcPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "rpc",
              "description": "Some description",
              "lxm": ["foobar"],
              "aud": "blah",
              "inheritAud": true
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(rpcPermissionFieldJson)

        val expected = RpcPermissionField(
            description = "Some description",
            resource = "rpc",
            lxm = listOf("foobar"),
            aud = "blah",
            inheritAud = true
        )

        assertIs<RpcPermissionField>(result)
        assertIs<SubTypeField>(result)
        assertIs<PermissionField>(result)
        assertEquals(expected, result)
    }
}

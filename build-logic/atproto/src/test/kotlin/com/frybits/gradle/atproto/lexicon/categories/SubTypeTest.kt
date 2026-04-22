package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.utils.lexiconJson
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun `params field deserialization minimal`() {
        @Language("JSON")
        val paramsFieldJson = """
            {
              "type": "params",
              "properties": {}
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(paramsFieldJson)

        val expected = ParamsField(
            properties = emptyMap()
        )

        assertIs<ParamsField>(result)
        assertIs<SubTypeField>(result)
        assertIsNot<PermissionField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `params field deserialization with non-valid property object`() {
        @Language("JSON")
        val paramsFieldJson = """
            {
              "type": "params",
              "properties": {
                "did": {
                  "type": "blob"
                }              
              }
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(paramsFieldJson)
        }
        assertEquals("Expected properties to contain type of [boolean, integer, string] in elements [{\"type\":\"blob\"}]", failure.message)
    }

    @Test
    fun `repo permission field deserialization`() {
        @Language("JSON")
        val repoPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "repo",
              "description": "Some description",
              "collection": ["blah"],
              "action": ["delete"]
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(repoPermissionFieldJson)

        val expected = RepoPermissionField(
            description = "Some description",
            resource = "repo",
            collection = setOf("blah"),
            action = setOf(Action.DELETE)
        )

        assertIs<RepoPermissionField>(result)
        assertIs<SubTypeField>(result)
        assertIs<PermissionField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `repo permission field deserialization minimal`() {
        @Language("JSON")
        val repoPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "repo",
              "collection": ["blah"]
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(repoPermissionFieldJson)

        val expected = RepoPermissionField(
            resource = "repo",
            collection = setOf("blah")
        )

        assertIs<RepoPermissionField>(result)
        assertIs<SubTypeField>(result)
        assertIs<PermissionField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `repo permission field deserialization with non-unique collection`() {
        @Language("JSON")
        val repoPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "repo",
              "collection": ["hello", "world", "hello"]
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(repoPermissionFieldJson)
        }
        assertEquals("Property 'collection' must have unique items", failure.message)
    }

    @Test
    fun `repo permission field deserialization with empty collection`() {
        @Language("JSON")
        val repoPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "repo",
              "collection": []
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(repoPermissionFieldJson)
        }
        assertEquals("Property 'collection' must not be empty", failure.message)
    }

    @Test
    fun `repo permission field deserialization with wildcard collection`() {
        @Language("JSON")
        val repoPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "repo",
              "collection": ["*"]
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(repoPermissionFieldJson)
        }
        assertEquals("Property 'collection' must not contain '*' wildcard", failure.message)
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
              "inheritAud": true
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(rpcPermissionFieldJson)

        val expected = RpcPermissionField(
            description = "Some description",
            resource = "rpc",
            lxm = listOf("foobar"),
            inheritAud = true
        )

        assertIs<RpcPermissionField>(result)
        assertIs<SubTypeField>(result)
        assertIs<PermissionField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `rpc permission field deserialization minimal`() {
        @Language("JSON")
        val rpcPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "rpc",
              "lxm": ["foobar"],
              "aud": "hello"
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(rpcPermissionFieldJson)

        val expected = RpcPermissionField(
            resource = "rpc",
            lxm = listOf("foobar"),
            aud = "hello"
        )

        assertIs<RpcPermissionField>(result)
        assertIs<SubTypeField>(result)
        assertIs<PermissionField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `rpc permission field deserialization with empty lxm`() {
        @Language("JSON")
        val rpcPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "rpc",
              "lxm": [],
              "aud": "*"
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(rpcPermissionFieldJson)
        }
        assertEquals("Property 'lxm' must not be empty", failure.message)
    }

    @Test
    fun `rpc permission field deserialization with wildcard in lxm`() {
        @Language("JSON")
        val rpcPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "rpc",
              "lxm": ["*"],
              "aud": "*"
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(rpcPermissionFieldJson)
        }
        assertEquals("Property 'lxm' must not contain '*' wildcard", failure.message)
    }

    @Test
    fun `rpc permission field deserialization with no aud or inheritAud`() {
        @Language("JSON")
        val rpcPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "rpc",
              "lxm": ["foobar"]
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(rpcPermissionFieldJson)
        }
        assertEquals("Property 'aud' must not be null", failure.message)
    }

    @Test
    fun `rpc permission field deserialization with no aud and inheritAud=true`() {
        @Language("JSON")
        val rpcPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "rpc",
              "lxm": ["foobar"],
              "inheritAud": true
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(rpcPermissionFieldJson)

        val expected = RpcPermissionField(
            resource = "rpc",
            lxm = listOf("foobar"),
            inheritAud = true
        )

        assertIs<RpcPermissionField>(result)
        assertIs<SubTypeField>(result)
        assertIs<PermissionField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `rpc permission field deserialization with non-wildcard aud and inheritAud=true`() {
        @Language("JSON")
        val rpcPermissionFieldJson = """
            {
              "type": "permission",
              "resource": "rpc",
              "lxm": ["foobar"],
              "aud": "hello",
              "inheritAud": true
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(rpcPermissionFieldJson)
        }
        assertEquals("Property 'aud' must be '*' wildcard or null with inheritAud", failure.message)
    }
}

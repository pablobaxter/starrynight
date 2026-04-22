package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.utils.lexiconJson
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertIsNot

class PrimaryTest {

    @Test
    fun `record field deserialization`() {
        @Language("JSON")
        val recordFieldJson = """
            {
              "type": "record",
              "description": "Some description",
              "key": "a key",
              "record": {
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
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(recordFieldJson)

        val expected = RecordField(
            description = "Some description",
            key = "a key",
            record = ObjectField(
                description = "Some description",
                properties = mapOf("foobar" to CidLinkField()),
                required = listOf("foobar"),
                nullable = listOf(),
            )
        )

        assertIs<RecordField>(result)
        assertIs<PrimaryField>(result)
        assertIsNot<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `record field deserialization minimal`() {
        @Language("JSON")
        val recordFieldJson = """
            {
              "type": "record",
              "key": "a key",
              "record": {
                  "type": "object",
                  "properties": {}
              }
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(recordFieldJson)

        val expected = RecordField(
            key = "a key",
            record = ObjectField(
                properties = emptyMap(),
            )
        )

        assertIs<RecordField>(result)
        assertIs<PrimaryField>(result)
        assertIsNot<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `record field deserialization fails with non-object record`() {
        @Language("JSON")
        val recordFieldJson = """
            {
              "type": "record",
              "key": "a key",
              "record": {
                  "type": "string"
              }
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(recordFieldJson)
        }
        assertEquals("Record expected 'object' type, got string", failure.message)
    }

    @Test
    fun `query field deserialization`() {
        @Language("JSON")
        val queryFieldJson = """
            {
              "type": "query",
              "description": "Some description",
              "parameters": { 
                "type": "params",
                "description": "Some description",
                "properties": {
                  "foobar": {
                    "type": "boolean"
                  }
                }
              },
              "output": {
                "description": "Some description",
                "encoding": "application/json",
                "schema": {
                  "type": "ref",
                  "ref": "foobar"
                }
              },
              "errors": [{
                "description": "Some description",
                "name": "MyError"
              }]
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(queryFieldJson)

        val expected = QueryField(
            description = "Some description",
            parameters = ParamsField(
                description = "Some description",
                properties = mapOf("foobar" to BooleanField()),
            ),
            output = BodyField(
                description = "Some description",
                encoding = "application/json",
                schema = RefField(ref = "foobar")
            ),
            errors = listOf(ErrorBodyField(
                description = "Some description",
                name = "MyError"
            ))
        )

        assertIs<QueryField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `query field deserialization minimal`() {
        @Language("JSON")
        val queryFieldJson = """
            {"type": "query"}
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(queryFieldJson)

        val expected = QueryField()

        assertIs<QueryField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `query field deserialization with non-params parameters`() {
        @Language("JSON")
        val queryFieldJson = """
            {
              "type": "query",
              "parameters": { 
                "type": "string"
              }
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(queryFieldJson)
        }
        assertEquals("XRPC expected 'params' type, got string", failure.message)
    }

    @Test
    fun `procedure field deserialization`() {
        @Language("JSON")
        val procedureFieldJson = """
            {
              "type": "procedure",
              "description": "Some description",
              "parameters": { 
                "type": "params",
                "description": "Some description",
                "properties": {
                  "foobar": {
                    "type": "boolean"
                  }
                }
              },
              "output": {
                "description": "Some description",
                "encoding": "application/json",
                "schema": {
                  "type": "ref",
                  "ref": "foobar"
                }
              },
              "errors": [{
                "description": "Some description",
                "name": "MyError"
              }],
              "input": {
                "description": "Some description",
                "encoding": "application/json",
                "schema": {
                  "type": "ref",
                  "ref": "blah"
                }
              }
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(procedureFieldJson)

        val expected = ProcedureField(
            description = "Some description",
            parameters = ParamsField(
                description = "Some description",
                properties = mapOf("foobar" to BooleanField()),
            ),
            output = BodyField(
                description = "Some description",
                encoding = "application/json",
                schema = RefField(ref = "foobar")
            ),
            errors = listOf(ErrorBodyField(
                description = "Some description",
                name = "MyError"
            )),
            input = BodyField(
                description = "Some description",
                encoding = "application/json",
                schema = RefField(ref = "blah")
            )
        )

        assertIs<ProcedureField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `procedure field deserialization minimal`() {
        @Language("JSON")
        val procedureFieldJson = """
            {"type": "procedure"}
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(procedureFieldJson)

        val expected = ProcedureField()

        assertIs<ProcedureField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `procedure field deserialization with non-params parameters`() {
        @Language("JSON")
        val procedureFieldJson = """
            {
              "type": "procedure",
              "parameters": { 
                "type": "string"
              }
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(procedureFieldJson)
        }
        assertEquals("XRPC expected 'params' type, got string", failure.message)
    }

    @Test
    fun `subscription field deserialization`() {
        @Language("JSON")
        val subscriptionFieldJson = """
            {
              "type": "subscription",
              "description": "Some description",
              "parameters": { 
                "type": "params",
                "description": "Some description",
                "properties": {
                  "foobar": {
                    "type": "boolean"
                  }
                }
              },
              "message": {
                "description": "Some description",
                "schema": {
                  "type": "union",
                  "refs": ["blah"]
                }
              },
              "errors": [{
                "description": "Some description",
                "name": "MyError"
              }]
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(subscriptionFieldJson)

        val expected = SubscriptionField(
            description = "Some description",
            parameters = ParamsField(
                description = "Some description",
                properties = mapOf("foobar" to BooleanField()),
            ),
            message = MessageField(
                description = "Some description",
                schema = UnionField(refs = listOf("blah"))
            ),
            errors = listOf(ErrorBodyField(
                description = "Some description",
                name = "MyError"
            )),
        )

        assertIs<SubscriptionField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `subscription field deserialization minimal`() {
        @Language("JSON")
        val subscriptionFieldJson = """
            {
              "type": "subscription",
              "message": {
                "schema": {
                  "type": "union",
                  "refs": ["blah"]
                }
              }
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(subscriptionFieldJson)

        val expected = SubscriptionField(
            message = MessageField(
                schema = UnionField(refs = listOf("blah"))
            ),
        )

        assertIs<SubscriptionField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `subscription field deserialization with non-union schema`() {
        @Language("JSON")
        val subscriptionFieldJson = """
            {
              "type": "subscription",
              "message": {
                "schema": {
                  "type": "ref",
                  "ref": "blah"
                }
              }
            }
        """.trimIndent()

        val failure = assertFailsWith<IllegalArgumentException> {
            lexiconJson.decodeFromString<LexiconType>(subscriptionFieldJson)
        }

        assertEquals("Response expected schema type 'union', got ref", failure.message)
    }

    @Test
    fun `permission set field deserialization`() {
        @Language("JSON")
        val permissionSetFieldJson = """
            {
              "type": "permission-set",
              "description": "Some description",
              "title": "foobar",
              "title:lang": {},
              "detail": "a detail",
              "detail:lang": { "blah": "foobar" },
              "permissions": [
                {
                  "type": "permission",
                  "resource": "repo",
                  "collection": ["hello", "world"],
                  "action": ["create"]
                }              
              ]
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(permissionSetFieldJson)

        val expected = PermissionSetField(
            description = "Some description",
            title = "foobar",
            titleLang = emptyMap(),
            detail = "a detail",
            detailLang = mapOf("blah" to "foobar"),
            permissions = listOf(RepoPermissionField(
                resource = "repo",
                collection = setOf("hello", "world"),
                action = setOf(Action.CREATE)
            ))
        )

        assertIs<PermissionSetField>(result)
        assertIs<PrimaryField>(result)
        assertIsNot<XRPCField>(result)
        assertEquals(expected, result)
    }

    @Test
    fun `permission set field deserialization minimal`() {
        @Language("JSON")
        val permissionSetFieldJson = """
            {
              "type": "permission-set",
              "permissions": []
            }
        """.trimIndent()

        val result = lexiconJson.decodeFromString<LexiconType>(permissionSetFieldJson)

        val expected = PermissionSetField(
            permissions = emptyList(),
        )

        assertIs<PermissionSetField>(result)
        assertIs<PrimaryField>(result)
        assertIsNot<XRPCField>(result)
        assertEquals(expected, result)
    }
}

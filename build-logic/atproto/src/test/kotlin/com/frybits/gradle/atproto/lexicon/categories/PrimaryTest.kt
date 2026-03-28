package com.frybits.gradle.atproto.lexicon.categories

import com.frybits.gradle.atproto.lexicon.lexiconJson
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals
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
                "type": "boolean"
              },
              "errors": [{
                "type": "string"
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
            output = BooleanField(),
            errors = listOf(StringField())
        )

        assertIs<QueryField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
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
                "type": "boolean"
              },
              "errors": [{
                "type": "string"
              }],
              "input": {
                "type": "integer"
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
            output = BooleanField(),
            errors = listOf(StringField()),
            input = IntegerField()
        )

        assertIs<ProcedureField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
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
                "type": "string"
              },
              "errors": [{
                "type": "string"
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
            message = StringField(),
            errors = listOf(StringField()),
        )

        assertIs<SubscriptionField>(result)
        assertIs<PrimaryField>(result)
        assertIs<XRPCField>(result)
        assertEquals(expected, result)
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
                collection = listOf("hello", "world"),
                action = setOf(Action.CREATE)
            ))
        )

        assertIs<PermissionSetField>(result)
        assertIs<PrimaryField>(result)
        assertIsNot<XRPCField>(result)
        assertEquals(expected, result)
    }
}

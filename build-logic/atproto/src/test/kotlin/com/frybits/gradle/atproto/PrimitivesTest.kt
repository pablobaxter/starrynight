/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 Pablo Baxter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.frybits.gradle.atproto

import com.frybits.gradle.atproto.lexicon.LexiconType
import com.frybits.gradle.atproto.lexicon.lexiconSerializerModule
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class PrimitivesTest {

    lateinit var json: Json

    @BeforeTest
    fun setup() {
        json = Json { serializersModule = lexiconSerializerModule }
    }

    @Test
    fun testPrimitive() {
        val blah = """
            {
                "type": "params",
                "required": ["subjects"],
                "properties": {
                    "subjects": {
                        "type": "array",
                        "maxLength": 100,
                        "minLength": 1,
                        "items": {
                          "type": "string"
                        }
                    }
                }
            }
        """.trimIndent()

        val result = json.decodeFromString<LexiconType>(blah)

        println(result)
    }

    @Test
    fun testRepoPermission() {
        val blah = """
            {
              "type": "permission",
              "resource": "repo",
              "collection": ["app.example.profile"]
            }
        """.trimIndent()

        val result = json.decodeFromString<LexiconType>(blah)
        println(result)
    }
}

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

package com.frybits.gradle.atproto.generator.context

import com.frybits.gradle.atproto.lexicon.Lexicon
import com.frybits.gradle.atproto.lexicon.RecordResponse
import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.frybits.gradle.atproto.utils.LexiconRef
import com.frybits.gradle.atproto.utils.lexiconJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.File

internal data class LexiconEnvironment(
    val recordFiles: List<File>,
    val outputDirectory: File
) {

    private val schemaById = recordFiles.associateBy { it.name }
    private val schemaCache = hashMapOf<String, Lexicon>()

    @OptIn(ExperimentalSerializationApi::class)
    fun loadLexicon(schemaId: String): Lexicon {
        return schemaCache.getOrPut(schemaId) {
            val record = requireNotNull(schemaById[schemaId]).inputStream().use { lexiconJson.decodeFromStream<RecordResponse>(it) }
            return@getOrPut record.value
        }
    }

    fun loadReference(
        source: Lexicon,
        reference: LexiconRef
    ): LexiconType {
        require(reference.isReference) { "Unable to call loadReference on non-reference string: $reference" }
        val schemaId = reference.schemaId
        val lexicon = if (schemaId.isBlank()) {
            source
        } else {
            loadLexicon(schemaId)
        }

        return requireNotNull(lexicon.defs[reference.objectRef])
    }
}

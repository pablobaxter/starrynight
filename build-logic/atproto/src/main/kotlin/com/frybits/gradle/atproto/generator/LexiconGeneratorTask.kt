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

package com.frybits.gradle.atproto.generator

import com.frybits.gradle.atproto.lexicon.RecordResponse
import com.frybits.gradle.atproto.lexicon.categories.PermissionSetField
import com.frybits.gradle.atproto.lexicon.categories.PrimaryField
import com.frybits.gradle.atproto.lexicon.categories.ProcedureField
import com.frybits.gradle.atproto.lexicon.categories.QueryField
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.SubscriptionField
import com.frybits.gradle.atproto.lexicon.lexiconJson
import com.squareup.kotlinpoet.ClassName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.work.Incremental
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

@CacheableTask
public abstract class LexiconGeneratorTask: DefaultTask() {

    init {
        group = "build"
        description = "Generate ATProto Lexicons Source Files"
    }

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Incremental
    public abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    public abstract val generatedSources: DirectoryProperty

    @OptIn(ExperimentalSerializationApi::class)
    @TaskAction
    internal fun generateSources() {
        val toGenerate = hashSetOf<ClassName>()
        val records = inputDirectory.asFileTree.files.associate { file ->
            val record = file.inputStream().use { lexiconJson.decodeFromStream<RecordResponse>(it) }
            return@associate record.value.id to record
        }

        records.values.map { it.value }.forEach { lexicon ->
            val lexiconType = lexicon.defs["main"] ?: return@forEach
            if (lexiconType !is PrimaryField) return@forEach
            val className = ClassName(lexicon.id, lexicon.id.split('.').last().capitalized())
            when (lexiconType) {
                is RecordField -> {
                    val fileSpec = lexiconType.generateClass(className, toGenerate)
                    generatedSources.file(fileSpec.relativePath).get().asFile.toPath().createParentDirectories().writeText(fileSpec.toString())
                }
                is PermissionSetField -> TODO()
                is ProcedureField -> TODO()
                is QueryField -> TODO()
                is SubscriptionField -> TODO()
            }
        }
    }
}

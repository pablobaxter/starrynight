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

package com.frybits.gradle.atproto.tasks

import com.frybits.gradle.atproto.generator.builder.generateClass
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.categories.ProcedureField
import com.frybits.gradle.atproto.lexicon.categories.QueryField
import com.frybits.gradle.atproto.lexicon.categories.SubscriptionField
import com.frybits.gradle.atproto.lexicon.categories.XRPCField
import com.frybits.gradle.atproto.utils.titleCaseFirstChar
import kotlinx.serialization.ExperimentalSerializationApi
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental

@CacheableTask
public abstract class LexiconGeneratorTask: DefaultTask() {

    init {
        group = "build"
        description = "Generate ATProto Lexicons Source Files"
    }

    @get:Input
    public abstract val nsids: SetProperty<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Incremental
    public abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    public abstract val generatedSources: DirectoryProperty

    @OptIn(ExperimentalSerializationApi::class)
    @TaskAction
    internal fun generateSources() {
        val environment = LexiconEnvironment(
            inputDirectory.asFileTree.files.toList(),
            generatedSources.get()
        )

        nsids.get().forEach { schemaId ->
            val lexicon = environment.loadLexicon(schemaId)

            val lexiconType = requireNotNull(lexicon.defs["main"]) { "No main definition in requested lexicon $schemaId" }
            require(lexiconType is XRPCField) { "Only lexicons with XRPC calls can be requested. SchemaId: $schemaId" }

            val name = schemaId.split('.').last().titleCaseFirstChar()

            val context = LexiconContext(name = name, lexicon = lexicon)

            when (lexiconType) {
                is ProcedureField -> generateClass(lexiconType, context, environment)
                is QueryField -> TODO()
                is SubscriptionField -> TODO()
            }
        }
    }
}

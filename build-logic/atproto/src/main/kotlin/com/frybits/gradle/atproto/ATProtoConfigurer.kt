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

import com.frybits.gradle.atproto.generator.LexiconGeneratorTask
import com.frybits.gradle.atproto.pull.LexiconPullTask
import com.frybits.gradle.core.Configurer
import com.frybits.gradle.core.configurations.baseProjectConfiguration
import com.frybits.gradle.core.configurations.jvmProjectConfiguration
import com.frybits.gradle.core.configurations.kotlinProjectConfiguration
import com.frybits.gradle.core.definitions.ATProtoLibrary
import com.frybits.gradle.core.definitions.BuildFile
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import javax.inject.Inject

/**
 * Handles configuration of the ATProto library project
 */
public abstract class ATProtoConfigurer @Inject internal constructor(
    private val project: Project
): Configurer {

    override fun configureBuild(buildFile: BuildFile) {
        require(buildFile is ATProtoLibrary) { "Attempting to configure ${buildFile::class} with ATProto configurations" }
        with(project) {
            baseProjectConfiguration(buildFile) // All base project configuration
            jvmProjectConfiguration() // All JVM configuration
            kotlinProjectConfiguration() // All Kotlin configuration

            val lexiconPullTask = tasks.register<LexiconPullTask>("lexiconPull") {
                endpoint.set("bsky.social")
                outputDir.set(layout.buildDirectory.dir("lexicons"))
                nsids.set(buildFile.lexicons)
            }

            val lexiconGeneratedTask = tasks.register<LexiconGeneratorTask>("lexiconGenerate") {
                // TODO cleanup
//                inputDirectory.set(lexiconPullTask.map { it.outputDir.get() })
                inputDirectory.set(layout.projectDirectory.dir("blah"))
                generatedSources.set(layout.buildDirectory.dir("generated/lexicons"))
            }

            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            kotlinExtension.sourceSets.getByName("main").generatedKotlin.srcDirs(lexiconGeneratedTask)
        }
    }
}

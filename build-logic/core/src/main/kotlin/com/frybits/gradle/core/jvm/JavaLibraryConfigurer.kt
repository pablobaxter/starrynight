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

package com.frybits.gradle.core.jvm

import com.frybits.gradle.core.Configurer
import com.frybits.gradle.core.configurations.baseProjectConfiguration
import com.frybits.gradle.core.configurations.baseProjectPlugins
import com.frybits.gradle.core.configurations.jvmProjectConfiguration
import com.frybits.gradle.core.configurations.kotlinProjectConfiguration
import com.frybits.gradle.core.configurations.kotlinProjectPlugins
import com.frybits.gradle.core.definitions.BuildFile
import com.frybits.gradle.core.definitions.JavaLibraryBuildFile
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import javax.inject.Inject

/**
 * Handles configuration of Java library projects
 */
public abstract class JavaLibraryConfigurer @Inject internal constructor(
    private val project: Project
): Configurer {

    override fun applyPlugins(buildFile: BuildFile) {
        require(buildFile is JavaLibraryBuildFile) { "Attempting to configure ${buildFile::class} with JavaLibrary configurations" }
        with(project) {
            apply(plugin = "org.jetbrains.kotlin.jvm")
            kotlinProjectPlugins(buildFile)
            baseProjectPlugins(buildFile)
        }
    }

    override fun configureBuild(buildFile: BuildFile) {
        require(buildFile is JavaLibraryBuildFile) { "Attempting to configure ${buildFile::class} with JavaLibrary configurations" }
        with(project) {
            jvmProjectConfiguration() // All JVM configuration
            kotlinProjectConfiguration(buildFile) // All Kotlin configuration
            baseProjectConfiguration(buildFile) // All base project configuration
        }
    }
}

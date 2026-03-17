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

package com.frybits.gradle.plugins

import com.frybits.gradle.configurations.configureRootProject
import com.frybits.gradle.definitions.BuildFile
import com.frybits.gradle.utils.isRoot
import kotlinx.serialization.json.Json
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Generic plugin that is applied to all projects
 *
 * id: com.frybits.plugin
 */
internal class FrybitsPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        if (isRoot) {
            configureRootProject()
        } else {
            val buildScript = layout.projectDirectory.file("build.gradle").asFile
            val kotlinBuildScript = layout.projectDirectory.file("build.gradle.kts").asFile
            require(!buildScript.exists() && !kotlinBuildScript.exists()) { "Gradle build scripts are not allowed" }
            val buildFile = providers.fileContents(layout.projectDirectory.file("build.json"))
                .asText
                .map { Json.decodeFromString<BuildFile>(it) }

            if (!buildFile.isPresent) {
                // Ignore intermediate projects
                return@run
            }
            logger.lifecycle(buildFile.get().toString())
        }
    }
}

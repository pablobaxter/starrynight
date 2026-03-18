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

package com.frybits.gradle.configurations.core

import com.frybits.gradle.definitions.BuildFile
import kotlinx.serialization.json.Json
import org.gradle.api.Project

/**
 * Entry for configuration that will apply to all projects (excluding root)
 */
internal fun Project.projectConfiguration() {
    requireNoBuildScripts()

    // Intermediate projects will not have build files, so skip those
    val buildFile = buildJson() ?: return

    logger.lifecycle(buildFile.toString())
}

// Returns the [BuildFile] object generated from the build.json file
private fun Project.buildJson(): BuildFile? {
    val buildFile = providers.fileContents(layout.projectDirectory.file("build.json"))
        .asText
        .map { Json.decodeFromString<BuildFile>(it) }

    return buildFile.orNull
}

// Ensure no build scripts are used for any project
private fun Project.requireNoBuildScripts() {
    val buildScript = layout.projectDirectory.file("build.gradle").asFile
    val kotlinBuildScript = layout.projectDirectory.file("build.gradle.kts").asFile
    require(!buildScript.exists() && !kotlinBuildScript.exists()) { "Gradle build scripts are not allowed" }
}

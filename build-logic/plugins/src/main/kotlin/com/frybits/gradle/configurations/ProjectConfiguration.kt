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

package com.frybits.gradle.configurations

import com.android.build.api.AndroidPluginVersion
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.frybits.gradle.android.configurations.AGP8Configurer
import com.frybits.gradle.android.configurations.AGP9Configurer
import com.frybits.gradle.atproto.ATProtoConfigurer
import com.frybits.gradle.core.Configurer
import com.frybits.gradle.core.definitions.ATProtoLibrary
import com.frybits.gradle.core.definitions.AndroidAppBuildFile
import com.frybits.gradle.core.definitions.AndroidLibraryBuildFile
import com.frybits.gradle.core.definitions.BuildFile
import com.frybits.gradle.core.definitions.JavaLibraryBuildFile
import com.frybits.gradle.core.jvm.JavaLibraryConfigurer
import dev.eav.tomlkt.Toml
import kotlinx.serialization.decodeFromString
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance

/**
 * Entry for configuration that will apply to all projects (excluding root) before they are evaluated
 */
internal fun Project.configurePlugins() {
    // No build scripts allowed!
    requireNoBuildScripts()

    // Intermediate projects will not have build files, so skip those
    val buildFile = populateBuildFile() ?: return

    val configurer = getConfigurer(buildFile, applyBuildPlugins = true)

    configurer.applyPlugins(buildFile)
}

/**
 * Entry for configuration that will apply to all projects (excluding root) after they are evaluated
 */
internal fun Project.configureBuilds() {
    // No build scripts allowed!
    requireNoBuildScripts()

    // Intermediate projects will not have build files, so skip those
    val buildFile = populateBuildFile() ?: return

    val configurer = getConfigurer(buildFile)

    configurer.configureBuild(buildFile)
}

private fun Project.getConfigurer(buildFile: BuildFile, applyBuildPlugins: Boolean = false): Configurer {
    return when (buildFile) {
        is AndroidAppBuildFile -> {
            if (applyBuildPlugins) {
                apply<AppPlugin>()
            }
            getAndroidConfigurer()
        }
        is AndroidLibraryBuildFile -> {
            if (applyBuildPlugins) {
                apply<LibraryPlugin>()
            }
            getAndroidConfigurer()
        }
        is JavaLibraryBuildFile -> {
            if (applyBuildPlugins) {
                apply<JavaLibraryPlugin>()
            }
            objects.newInstance<JavaLibraryConfigurer>()
        }
        is ATProtoLibrary -> {
            if (applyBuildPlugins) {
                apply<JavaLibraryPlugin>()
            }
            objects.newInstance<ATProtoConfigurer>()
        }
    }
}

// Gets the current AGP configurer for project configuration
private fun Project.getAndroidConfigurer(): Configurer {
    val androidCurrentVersion = AndroidPluginVersion.getCurrent()
    return when(androidCurrentVersion.major) {
        8 -> {
            objects.newInstance<AGP8Configurer>(
                extensions.getByName("androidComponents")
            )
        }
        9 -> {
            objects.newInstance<AGP9Configurer>(
                extensions.getByName("androidComponents")
            )
        }
        else -> throw Exception("Unsupported AGP version: $androidCurrentVersion")
    }
}

// Returns the [BuildFile] object generated from the build.json file
private fun Project.populateBuildFile(): BuildFile? {
    val buildFile = providers.fileContents(layout.projectDirectory.file("build.toml"))
        .asText
        .map { Toml.decodeFromString<BuildFile>(it) }

    return buildFile.orNull
}

// Ensure no build scripts are used for any project
private fun Project.requireNoBuildScripts() {
    require(!buildFile.exists()) { "$path - Gradle build scripts are not allowed" }
}

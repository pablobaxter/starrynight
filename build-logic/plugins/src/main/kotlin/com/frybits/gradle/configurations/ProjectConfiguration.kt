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

import com.akuleshov7.ktoml.Toml
import com.android.build.api.AndroidPluginVersion
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.frybits.gradle.android.configurations.AGP8Configurer
import com.frybits.gradle.android.configurations.AGP9Configurer
import com.frybits.gradle.core.Configurer
import com.frybits.gradle.core.jvm.JavaConfigurer
import com.frybits.gradle.core.definitions.BuildFile
import com.frybits.gradle.core.definitions.ProjectType
import kotlinx.serialization.decodeFromString
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance

/**
 * Entry for configuration that will apply to all projects (excluding root)
 */
internal fun Project.projectConfiguration() {
    // No build scripts allowed!
    requireNoBuildScripts()

    // Intermediate projects will not have build files, so skip those
    val buildFile = populateBuildFile() ?: return

    // Configures based on build file
    buildFileConfiguration(buildFile)
}

/**
 * Configure based off the provided [BuildFile]
 */
private fun Project.buildFileConfiguration(buildFile: BuildFile) {
    val configurer = when (buildFile.type) {
        ProjectType.ANDROID_APPLICATION -> {
            apply<AppPlugin>()
            enableKotlinPluginIfNeeded()
            getAndroidConfigurer()
        }
        ProjectType.ANDROID_LIBRARY -> {
            apply<LibraryPlugin>()
            enableKotlinPluginIfNeeded()
            getAndroidConfigurer()
        }
        ProjectType.JAVA_LIBRARY -> {
            apply<JavaLibraryPlugin>()
            apply(plugin = "org.jetbrains.kotlin.jvm")
            objects.newInstance<JavaConfigurer>()
        }
    }

    configurer.configureBuild(buildFile)
}

private fun Project.enableKotlinPluginIfNeeded() {
    val androidCurrentVersion = AndroidPluginVersion.getCurrent()
    if (androidCurrentVersion.major < 9) {
        apply(plugin = "org.jetbrains.kotlin.android")
    }
}

private fun Project.getAndroidConfigurer(): Configurer {
    val androidCurrentVersion = AndroidPluginVersion.getCurrent()
    return when(androidCurrentVersion.major) {
        8 -> {
            objects.newInstance<AGP8Configurer>(
                extensions.getByName("android"),
                extensions.getByName("androidComponents")
            )
        }
        9 -> {
            objects.newInstance<AGP9Configurer>(
                extensions.getByName("android"),
                extensions.getByName("androidComponents")
            )
        }
        else -> throw Exception()
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
    val buildScript = layout.projectDirectory.file("build.gradle").asFile
    val kotlinBuildScript = layout.projectDirectory.file("build.gradle.kts").asFile
    require(!buildScript.exists() && !kotlinBuildScript.exists()) { "Gradle build scripts are not allowed" }
}

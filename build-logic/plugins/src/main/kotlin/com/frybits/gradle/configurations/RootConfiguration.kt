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
import com.android.tools.r8.Version
import com.frybits.gradle.utils.isRoot
import com.google.devtools.ksp.gradle.KSP_VERSION
import org.gradle.api.Project
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion

/**
 * Configuration that will only apply to the root before it is evaluated
 */
internal fun Project.rootBeforeProjectConfiguration() {
    require(isRoot) { "This method should only be used with the root project" }
    logVersions()
    checkForBuildFile()
}

/**
 * Configuration that will only apply to the root after it is evaluated
 */
internal fun Project.rootAfterProjectConfiguration() {
    require(isRoot) { "This method should only be used with the root project" }
}

// Helper function to log the versions of major tools used for the build
private fun Project.logVersions() {
    val list = buildMap {
        put("Gradle", GradleVersion.current().version)
        put("AGP", AndroidPluginVersion.getCurrent().version)
        put("R8", Version.getVersionString().substringBefore(" "))
        put("Kotlin", kotlinToolingVersion)
        put("KSP", KSP_VERSION)
    }.map { (name, version) ->
        return@map "$name: $version"
    }

    logger.lifecycle("Versions: ${list.joinToString(separator = ", ", prefix = "[ ", postfix = " ]")}")
}

// Ensure that build file does not exist
private fun Project.checkForBuildFile() {
    require(!layout.projectDirectory.file(buildFile.name).asFile.exists()) { "Root build file should not exist" }
}

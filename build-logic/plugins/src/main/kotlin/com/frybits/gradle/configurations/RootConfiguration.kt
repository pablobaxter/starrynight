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
import java.security.MessageDigest

/**
 * Configuration that will only apply to the root
 */
internal fun Project.rootProjectConfiguration() {
    require(isRoot) { "This method should only be used with the root project" }
    logVersions()
    checkBuildFileModification()
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

// Ensure that nothing new is added to the root build file
private fun Project.checkBuildFileModification() {
    val digest = MessageDigest.getInstance("SHA-256")
    val buildFileUnmodified = providers.fileContents(layout.projectDirectory.file(buildFile.name)).asBytes
        .zip(providers.gradleProperty("com.frybits.root.build.hash")) { bytes, hash ->
            return@zip digest.digest(bytes).toHexString() == hash
        }

    require(buildFileUnmodified.get()) { "$buildFile should not be modified" }
}

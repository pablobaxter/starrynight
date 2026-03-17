/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 pablo
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
import com.frybits.gradle.utils.isRoot
import org.gradle.api.Project
import org.gradle.util.GradleVersion

internal fun Project.configureRootProject() {
    require(isRoot) { "This method should only be used with the root project" }
    logVersions()
}


private fun Project.logVersions() {
    val list = buildMap {
        put("Gradle", GradleVersion.current().version)
        put("AGP", AndroidPluginVersion.getCurrent().version)
    }.map { (name, version) ->
        return@map "$name: $version"
    }

    logger.lifecycle("Versions: ${list.joinToString(separator = ", ", prefix = "[ ", postfix = " ]")}")
}

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

package com.frybits.gradle.android.configurations.library

import com.android.build.api.dsl.LibraryExtension
import com.frybits.gradle.core.definitions.AndroidLibraryBuildFile
import com.frybits.gradle.core.definitions.BuildFile
import org.gradle.api.Project

/**
 * Configures Android Library projects
 */
public fun Project.androidLibraryConfiguration(buildFile: BuildFile, android: LibraryExtension) {
    require(buildFile is AndroidLibraryBuildFile) { "Attempting to configure ${buildFile::class} with Android App configurations" }
    with(android) {
        defaultConfig {
            consumerProguardFile("consumer-proguard-rules.pro")
        }
    }
}

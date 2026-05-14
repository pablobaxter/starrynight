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

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryVariant
import com.android.build.api.variant.LibraryVariantBuilder
import com.frybits.gradle.core.definitions.AndroidBuildFile
import com.frybits.gradle.core.definitions.AndroidLibraryBuildFile
import org.gradle.api.Project

private val AGP_9_2_1 = AndroidPluginVersion(9, 2, 1)

/**
 * Configures Android Library projects
 */
public fun Project.androidLibraryConfiguration(buildFile: AndroidBuildFile, android: LibraryExtension) {
    require(buildFile is AndroidLibraryBuildFile) { "Attempting to configure ${buildFile::class} with Android App configurations" }
    with (android) {
        defaultConfig {
            if (AndroidPluginVersion.getCurrent() < AGP_9_2_1) {
                consumerProguardFiles(layout.projectDirectory.file("consumer-proguard-rules.pro"))
            }
        }
    }
}

/**
 * Configures Android [LibraryVariantBuilder]
 */
public fun Project.androidLibraryVariantBuilderConfiguration(buildFile: AndroidBuildFile, variantBuilder: LibraryVariantBuilder) {
    require(buildFile is AndroidLibraryBuildFile) { "Attempting to configure ${buildFile::class} with Android App configurations" }
}

/**
 * Configures Android [LibraryVariant]
 */
public fun Project.androidLibraryVariantConfiguration(buildFile: AndroidBuildFile, variant: LibraryVariant) {
    require(buildFile is AndroidLibraryBuildFile) { "Attempting to configure ${buildFile::class} with Android App configurations" }
    if (AndroidPluginVersion.getCurrent() >= AGP_9_2_1) {
        @Suppress("UnstableApiUsage")
        variant.consumerProguardFiles.add(layout.projectDirectory.file("consumer-proguard-rules.pro"))
    }
}

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

package com.frybits.gradle.android.configurations.app

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.ApplicationExtension
import com.frybits.gradle.core.definitions.AndroidAppBuildFile
import com.frybits.gradle.core.definitions.BuildFile
import org.gradle.api.Project

/**
 * Configures Android Application projects
 */
public fun Project.androidAppConfiguration(buildFile: BuildFile, android: ApplicationExtension) {
    require(buildFile is AndroidAppBuildFile) { "Attempting to configure ${buildFile::class} with Android App configurations" }
    with(android) {
        defaultConfig {
            applicationId = buildFile.applicationId
            configureTargetSdk(buildFile)
        }

        buildTypes.configureEach {
            val minifyProperty = providers.gradleProperty("com.frybits.android.minify").map { it.toBoolean() }
            if (minifyProperty.isPresent) {
                isMinifyEnabled = minifyProperty.get()
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

private val AGP_8_13_0 = AndroidPluginVersion(8, 13)

private fun ApplicationDefaultConfig.configureTargetSdk(buildFile: AndroidAppBuildFile) {
    val pluginVersion = AndroidPluginVersion.getCurrent()
    if (pluginVersion >= AGP_8_13_0) {
        targetSdk {
            version = if (buildFile.previewTargetSdk.isNullOrBlank()) {
                release(requireNotNull(buildFile.targetSdk))
            } else {
                preview(requireNotNull(buildFile.previewTargetSdk))
            }
        }
    } else {
        if (buildFile.previewTargetSdk.isNullOrBlank()) {
            targetSdk = requireNotNull(buildFile.targetSdk)
        } else {
            targetSdkPreview = requireNotNull(buildFile.previewTargetSdk)
        }
    }
}

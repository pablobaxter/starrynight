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
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.ApplicationVariantBuilder
import com.android.build.api.variant.CanMinifyCodeBuilder
import com.android.build.api.variant.GeneratesApkBuilder
import com.frybits.gradle.core.definitions.AndroidAppBuildFile
import com.frybits.gradle.core.definitions.AndroidBuildFile
import org.gradle.api.Project

private val AGP_9 = AndroidPluginVersion(9, 0)
/**
 * Configures Android Application projects
 */
public fun Project.androidAppConfiguration(buildFile: AndroidBuildFile, android: ApplicationExtension) {
    require(buildFile is AndroidAppBuildFile) { "Attempting to configure ${buildFile::class} with Android App configurations" }

    // Only needed because AGP 8 defaults to proguard-android.txt
    if (AndroidPluginVersion.getCurrent() < AGP_9) {
        android.buildTypes.configureEach {
            proguardFiles(android.getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}

/**
 * Configures Android [ApplicationVariantBuilder]
 */
public fun Project.androidAppVariantBuilderConfiguration(buildFile: AndroidBuildFile, applicationVariantBuilder: ApplicationVariantBuilder) {
    require(buildFile is AndroidAppBuildFile) { "Attempting to configure ${buildFile::class} with Android App configurations" }
    configureTargetSdk(buildFile, applicationVariantBuilder)
    minifyCodeConfiguration(applicationVariantBuilder)
}

/**
 * Configures Android [ApplicationVariant]
 */
public fun Project.androidAppVariantConfiguration(buildFile: AndroidBuildFile, applicationVariant: ApplicationVariant) {
    require(buildFile is AndroidAppBuildFile) { "Attempting to configure ${buildFile::class} with Android App configurations" }
    with(applicationVariant) {
        applicationId.set(buildFile.applicationId)
        proguardFiles.add(layout.projectDirectory.file("proguard-rules.pro"))
        outputs.forEach { variantOutput ->
            variantOutput.versionCode.set(providers.gradleProperty("com.frybits.android.version.code").map { it.toInt() })
            variantOutput.versionName.set(providers.gradleProperty("com.frybits.android.version.name"))
        }
    }
}

private fun configureTargetSdk(buildFile: AndroidAppBuildFile, generatesApkBuilder: GeneratesApkBuilder) {
    with (generatesApkBuilder) {
        if (buildFile.previewTargetSdk.isNullOrBlank()) {
            targetSdk = requireNotNull(buildFile.targetSdk)
        } else {
            targetSdkPreview = requireNotNull(buildFile.previewTargetSdk)
        }
    }
}

@Suppress("UnstableApiUsage")
private fun Project.minifyCodeConfiguration(minifyCodeBuilder: CanMinifyCodeBuilder) {
    val minifyProvider = providers.gradleProperty("com.frybits.android.minify").map { it.toBoolean() }
    if (minifyProvider.isPresent) {
        minifyCodeBuilder.isMinifyEnabled = minifyProvider.get()
    }
}

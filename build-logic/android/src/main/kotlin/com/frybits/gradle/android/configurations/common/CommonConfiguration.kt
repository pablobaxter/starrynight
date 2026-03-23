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

package com.frybits.gradle.android.configurations.common

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import com.frybits.gradle.android.wrappers.CommonExtensionWrapper
import com.frybits.gradle.core.configurations.baseProjectConfiguration
import com.frybits.gradle.core.configurations.jvmProjectConfiguration
import com.frybits.gradle.core.configurations.kotlinProjectConfiguration
import com.frybits.gradle.core.definitions.AndroidBuildFile
import com.frybits.gradle.utils.androidSourceCompatibility
import com.frybits.gradle.utils.androidTargetCompatibility
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/**
 * Base configuration for all android builds
 */
public fun Project.androidBaseConfiguration(buildFile: AndroidBuildFile) {
    baseProjectConfiguration(buildFile) // All base project configuration
    jvmProjectConfiguration() // All JVM configuration
    kotlinProjectConfiguration() // All Kotlin configuration
    apply(plugin = "dev.zacsweers.metro")
}

/**
 * Configures the CommonExtension used by all Android projects.
 *
 * Note, this uses the [CommonExtensionWrapper], not the AGP [com.android.build.api.dsl.CommonExtension]
 */
public fun Project.androidCommonConfiguration(buildFile: AndroidBuildFile, android: CommonExtensionWrapper) {
    with(android) {
        namespace = buildFile.namespace ?: generateNamespace()
        configureCompileSdk(android)
        with(compileOptions) {
            targetCompatibility(androidTargetCompatibility.get())
            sourceCompatibility(androidSourceCompatibility.get())
        }
    }
}

/**
 * Configures the base [VariantBuilder]
 */
public fun Project.androidVariantBuilderConfiguration(buildFile: AndroidBuildFile, variantBuilder: VariantBuilder) {
    configureMinSdk(variantBuilder)
}

/**
 * Configures the base [Variant]
 */
public fun Project.androidVariantConfiguration(buildFile: AndroidBuildFile, variant: Variant) {
}

private val AGP_8_13_0 = AndroidPluginVersion(8, 13)
private val AGP_8_10_1 = AndroidPluginVersion(8, 10, 1)
private fun Project.configureCompileSdk(android: CommonExtensionWrapper) {
    with(android) {
        val pluginVersion = AndroidPluginVersion.getCurrent()

        val compileSdkProvider = providers.gradleProperty("com.frybits.android.compile.sdk").map { it.toInt() }
        val compileSdkExtensionProvider = providers.gradleProperty("com.frybits.android.compile.sdk.extension").map { it.toInt() }
        val compileSdkMinorProvider = providers.gradleProperty("com.frybits.android.compile.sdk.minor").map { it.toInt() }
        val previewCompileSdkProvider = providers.gradleProperty("com.frybits.android.compile.sdk.preview")

        if (pluginVersion >= AGP_8_13_0) {
            compileSdk {
                version = if (previewCompileSdkProvider.isPresent) {
                    preview(previewCompileSdkProvider.get())
                } else {
                    release(compileSdkProvider.get()) {
                        sdkExtension = compileSdkExtensionProvider.orNull
                        minorApiLevel = compileSdkMinorProvider.orNull
                    }
                }
            }
        } else {
            if (previewCompileSdkProvider.isPresent) {
                compileSdkPreview = previewCompileSdkProvider.get()
            } else {
                compileSdk = compileSdkProvider.orNull
                compileSdkExtension = compileSdkExtensionProvider.orNull

                if (pluginVersion >= AGP_8_10_1) {
                    compileSdkMinor = compileSdkMinorProvider.orNull
                }
            }
        }
    }
}

private fun Project.configureMinSdk(variantBuilder: VariantBuilder) {
    with(variantBuilder) {
        val minSdkProvider = providers.gradleProperty("com.frybits.android.min.sdk").map { it.toInt() }
        val previewMinSdkProvider = providers.gradleProperty("com.frybits.android.min.sdk.preview")

        if (previewMinSdkProvider.isPresent) {
            minSdkPreview = previewMinSdkProvider.get()
        } else {
            minSdk = minSdkProvider.get()
        }
    }
}

private fun Project.generateNamespace(): String {
    return "com.frybits.starrynight.android${path.replace(':', '.')}"
}

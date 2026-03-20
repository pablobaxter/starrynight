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

package com.frybits.gradle.configurations.core

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.frybits.gradle.configurations.configurers.JavaConfigurer
import com.frybits.gradle.configurations.configurers.getAndroidConfigurer
import com.frybits.gradle.definitions.BuildFile
import com.frybits.gradle.definitions.ProjectType
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance

/**
 * Configure based off the provided [BuildFile]
 */
internal fun Project.buildFileConfiguration(buildFile: BuildFile) {
    val configurer = when (buildFile.type) {
        ProjectType.ANDROID_APPLICATION -> {
            apply<AppPlugin>()
            apply(plugin = "org.jetbrains.kotlin.android")
            getAndroidConfigurer()
        }
        ProjectType.ANDROID_LIBRARY -> {
            apply<LibraryPlugin>()
            apply(plugin = "org.jetbrains.kotlin.android")
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

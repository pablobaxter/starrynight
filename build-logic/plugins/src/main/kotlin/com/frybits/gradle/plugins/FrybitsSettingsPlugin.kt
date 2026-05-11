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

package com.frybits.gradle.plugins

import com.frybits.gradle.configurations.configureBuilds
import com.frybits.gradle.configurations.configurePlugins
import com.frybits.gradle.configurations.rootAfterProjectConfiguration
import com.frybits.gradle.configurations.rootBeforeProjectConfiguration
import com.frybits.gradle.utils.isRoot
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode

/**
 * Entry plugin for settings
 *
 * id: com.frybits.plugin
 */
internal class FrybitsSettingsPlugin : Plugin<Settings> {

    override fun apply(target: Settings) = target.run {
        @Suppress("UnstableApiUsage")
        val projectList =
            providers.fileContents(layout.rootDirectory.dir("gradle").file("all-projects.txt"))
                .asText
                .map { text ->
                    return@map text.lines().toTypedArray()
                }
        include(*projectList.get())

        rootProject.name = providers.gradleProperty("com.frybits.name").get()

        @Suppress("UnstableApiUsage")
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
            }

            versionCatalogs {
                register("libs") {
                    version(
                        "agp-version",
                        providers.gradleProperty("com.frybits.agp.version").get()
                    )
                    version(
                        "kotlin-version",
                        providers.gradleProperty("com.frybits.kotlin.version").get()
                    )
                    version(
                        "ksp-version",
                        providers.gradleProperty("com.frybits.ksp.version").get()
                    )
                    version(
                        "metro-version",
                        providers.gradleProperty("com.frybits.metro.version").get()
                    )
                }
            }
        }

        @Suppress("UnstableApiUsage")
        gradle.lifecycle.beforeProject {
            if (isRoot) {
                rootBeforeProjectConfiguration()
            } else {
                configurePlugins()
            }
        }

        @Suppress("UnstableApiUsage")
        gradle.lifecycle.afterProject {
            if (isRoot) {
                rootAfterProjectConfiguration()
            } else {
                configureBuilds()
            }
        }
    }
}

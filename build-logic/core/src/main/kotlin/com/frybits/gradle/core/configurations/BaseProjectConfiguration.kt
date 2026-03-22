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

package com.frybits.gradle.core.configurations

import com.frybits.gradle.core.definitions.BuildFile
import com.frybits.gradle.core.definitions.Library
import com.frybits.gradle.core.definitions.Platform
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

/**
 * Configuration that should be common to all projects (excluding root)
 */
public fun Project.baseProjectConfiguration(buildFile: BuildFile) {
    handleDependencies(buildFile)
}

// AfterEvaluate needed to allow Gradle to register the version catalog extension, since we are running before the current project is evaluated
private fun Project.handleDependencies(buildFile: BuildFile) = afterEvaluate {
    val libs = the<VersionCatalogsExtension>().named("libs")
    dependencies {
        buildFile.dependencies.forEach { (configuration, deps) ->
            deps.forEach { dep ->
                val notation = when (dep) {
                    is com.frybits.gradle.core.definitions.Project -> {
                        project(dep.name)
                    }

                    is Library -> {
                        libs.findLibrary(dep.name).orElseThrow {
                            GradleException("Dependency ${dep.name} not found in version catalog ${libs.name}")
                        }
                    }

                    is Platform -> {
                        val module = when (dep.module) {
                            is com.frybits.gradle.core.definitions.Project -> {
                                project(dep.module.name)
                            }

                            is Library -> {
                                libs.findLibrary(dep.module.name).orElseThrow {
                                    GradleException("Dependency ${dep.module.name} not found in version catalog ${libs.name}")
                                }
                            }
                        }
                        platform(module)
                    }
                }

                add(configuration, notation)
            }
        }
    }
}

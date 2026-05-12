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
import com.google.protobuf.gradle.ProtobufExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Apply compose plugins for project if requested
 */
internal fun Project.handleProtobufPlugins(buildFile: BuildFile) {
    if (buildFile.protoBuf != null) {
        apply(plugin = "com.google.protobuf")
    }
}

/**
 * Configures compose for the project
 */
internal fun Project.handleProtobufConfiguration(buildFile: BuildFile) {
    val protoBuf = buildFile.protoBuf
    if (protoBuf != null) {
        configure<ProtobufExtension> {
            protoc {
                artifact = protoBuf.protocArtifact
            }
            generateProtoTasks {
                all().configureEach {
                    builtins {
                        register("java") {
                            option("lite")
                        }
                        register("kotlin")
                    }
                }
            }
        }
    }
}

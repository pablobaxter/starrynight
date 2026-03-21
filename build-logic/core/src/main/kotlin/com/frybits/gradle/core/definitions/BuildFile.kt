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

package com.frybits.gradle.core.definitions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Defines the structure of the build.toml file
 */
@Serializable
public sealed interface BuildFile {
    public val libraries: List<String>
    public val projects: List<String>
}

@Serializable
public sealed interface AndroidBuildFile: BuildFile {
    public val namespace: String?
}

@Serializable
@SerialName("androidApplication")
public data class AndroidAppBuildFile(
    val applicationId: String,
    val targetSdk: Int?,
    val previewTargetSdk: String? = null,
    override val libraries: List<String> = emptyList(),
    override val projects: List<String> = emptyList(),
    override val namespace: String? = null
): AndroidBuildFile

@Serializable
@SerialName("androidLibrary")
public data class AndroidLibraryBuildFile(
    override val libraries: List<String> = emptyList(),
    override val projects: List<String> = emptyList(),
    override val namespace: String? = null
): AndroidBuildFile

@Serializable
@SerialName("javaLibrary")
public data class JavaLibraryBuildFile(
    override val libraries: List<String> = emptyList(),
    override val projects: List<String> = emptyList()
): BuildFile

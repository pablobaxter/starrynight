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
import kotlinx.serialization.Transient

/**
 * Defines the structure of the build.toml file
 */
@Serializable
public sealed interface BuildFile {
    public val dependencies: Map<String, List<Dependency>>
    public val enableCompose: Boolean
    public val enableMetro: Boolean
    public val enableRoom: Boolean
    public val protoBuf: ProtoBuf?
}

@Serializable
public sealed interface AndroidBuildFile: BuildFile {
    public val namespace: String?
    public val buildConfigs: Map<String, List<BuildConfigField>>?
}

@Serializable
@SerialName("androidApplication")
public data class AndroidAppBuildFile(
    val applicationId: String,
    val targetSdk: Int?,
    val previewTargetSdk: String? = null,
    override val dependencies: Map<String, List<Dependency>> = emptyMap(),
    override val namespace: String? = null,
    override val enableCompose: Boolean = false,
    override val enableMetro: Boolean = false,
    override val enableRoom: Boolean = false,
    override val protoBuf: ProtoBuf? = null,
    override val buildConfigs: Map<String, List<BuildConfigField>>? = null
): AndroidBuildFile

@Serializable
@SerialName("androidLibrary")
public data class AndroidLibraryBuildFile(
    override val dependencies: Map<String, List<Dependency>> = emptyMap(),
    override val namespace: String? = null,
    override val enableCompose: Boolean = false,
    override val enableMetro: Boolean = false,
    override val enableRoom: Boolean = false,
    override val protoBuf: ProtoBuf? = null,
    override val buildConfigs: Map<String, List<BuildConfigField>>? = null
): AndroidBuildFile

@Serializable
@SerialName("javaLibrary")
public data class JavaLibraryBuildFile(
    override val dependencies: Map<String, List<Dependency>> = emptyMap(),
    override val enableCompose: Boolean = false,
    override val enableMetro: Boolean = false,
    override val enableRoom: Boolean = false,
    override val protoBuf: ProtoBuf? = null,
): BuildFile

@Serializable
@SerialName("atprotoLibrary")
public data class ATProtoLibrary(
    override val dependencies: Map<String, List<Dependency>>,
    val lexicons: List<String>,
    override val enableMetro: Boolean = false,
): BuildFile {

    @Transient
    override val enableCompose: Boolean = false

    @Transient
    override val enableRoom: Boolean = false

    @Transient
    override val protoBuf: ProtoBuf? = null
}

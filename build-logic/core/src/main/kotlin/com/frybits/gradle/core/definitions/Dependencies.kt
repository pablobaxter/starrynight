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

@Serializable
public sealed interface Dependency

@Serializable
public sealed interface Module: Dependency {
    public val name: String
}

/**
 * The [name] should be the project path
 */
@Serializable
@SerialName("project")
public data class Project(
    override val name: String
): Module

/**
 * The [name] should be the library name in the version catalog
 */
@Serializable
@SerialName("library")
public data class Library(
    override val name: String
): Module

/**
 * Performs the [org.gradle.kotlin.dsl.support.delegates.DependencyHandlerDelegate.platform] on the provided [Module]
 */
@Serializable
@SerialName("platform")
public data class Platform(
    val module: Module
): Dependency

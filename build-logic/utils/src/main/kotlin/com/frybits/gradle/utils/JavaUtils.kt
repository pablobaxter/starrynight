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

package com.frybits.gradle.utils

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider

public val Project.javaTargetCompatibility: Provider<JavaVersion>
    get() = providers.gradleProperty("com.frybits.java.compatibility.target")
        .map { target -> JavaVersion.toVersion(target) }

public val Project.javaSourceCompatibility: Provider<JavaVersion>
    get() = providers.gradleProperty("com.frybits.java.compatibility.source")
        .map { target -> JavaVersion.toVersion(target) }


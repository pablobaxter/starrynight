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

package com.frybits.gradle.configurations.configurers

import com.android.build.api.AndroidPluginVersion
import com.frybits.gradle.android.AGP9Configurer
import com.frybits.gradle.Configurer
import com.frybits.gradle.android.AGP8Configurer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.newInstance

internal fun Project.getAndroidConfigurer(): Configurer {
    val androidCurrentVersion = AndroidPluginVersion.getCurrent()
    return when(androidCurrentVersion.major) {
        8 -> {
            objects.newInstance<AGP8Configurer>(
                extensions.getByName("android"),
                extensions.getByName("androidComponents")
            )
        }
        9 -> {
            objects.newInstance<AGP9Configurer>(
                extensions.getByName("android"),
                extensions.getByName("androidComponents")
            )
        }
        else -> throw Exception()
    }
}

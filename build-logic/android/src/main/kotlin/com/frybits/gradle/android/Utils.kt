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

package com.frybits.gradle.android

import com.android.build.api.AndroidPluginVersion
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.hasPlugin

// Handles applying the kotlin plugin if AGP 9 is used
internal fun Project.enableKotlinPluginIfNeeded() {
    require(plugins.hasPlugin(AndroidBasePlugin::class)) { "This function should not be used on a non-android project" }
    val androidCurrentVersion = AndroidPluginVersion.getCurrent()
    if (androidCurrentVersion.major < 9) {
        apply(plugin = "org.jetbrains.kotlin.android")
    }
}

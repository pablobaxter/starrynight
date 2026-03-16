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

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply

/**
 * Setting plugin that provides all the plugins needed for any project defined.
 * Note: This is the only publicly available plugin. All others are added in the `gradle.beforeProject {}` block.
 *
 * id: com.frybits.settings
 */
class FrybitsSettingsPlugin : Plugin<Settings> {

    override fun apply(target: Settings) = target.run {
        gradle.beforeProject {
            @Suppress("UnstableApiUsage")
            if (isolated == isolated.rootProject) {
                apply<FrybitsRootPlugin>()
            }
        }
    }
}

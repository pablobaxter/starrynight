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

import com.frybits.gradle.Configurer
import com.frybits.gradle.configurations.baseProjectConfiguration
import com.frybits.gradle.configurations.jvmProjectConfiguration
import com.frybits.gradle.configurations.kotlinProjectConfiguration
import com.frybits.gradle.definitions.BuildFile
import org.gradle.api.Project
import javax.inject.Inject

internal abstract class JavaConfigurer @Inject internal constructor(
    private val project: Project
): Configurer {

    override fun configureBuild(buildFile: BuildFile) {
        with(project) {
            baseProjectConfiguration() // All base project configuration
            jvmProjectConfiguration() // All JVM configuration
            kotlinProjectConfiguration() // All Kotlin configuration
            TODO("Not yet implemented")
        }
    }
}

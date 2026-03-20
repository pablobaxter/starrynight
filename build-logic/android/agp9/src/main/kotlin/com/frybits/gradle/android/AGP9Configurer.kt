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

package com.frybits.gradle.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import com.frybits.gradle.Configurer
import com.frybits.gradle.definitions.BuildFile
import org.gradle.api.Project
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

public abstract class AGP9Configurer @Inject internal constructor(
    private val project: Project,
    private val commonExtension: CommonExtension,
    private val componentsExtension: AndroidComponentsExtension<CommonExtension, VariantBuilder, Variant>
): Configurer {

    override fun configureBuild(buildFile: BuildFile) {
        project.androidCommonConfiguration(buildFile, project.objects.newInstance<AGP9CommonExtensionWrapper>(commonExtension))

        when(commonExtension) {
            is ApplicationExtension -> {
                project.androidAppConfiguration(buildFile, commonExtension)
            }
            is LibraryExtension -> {
                project.androidLibraryConfiguration(buildFile, commonExtension)
            }
        }
    }
}

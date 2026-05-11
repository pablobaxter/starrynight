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

package com.frybits.gradle.android.configurations

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.ApplicationVariantBuilder
import com.android.build.api.variant.LibraryVariant
import com.android.build.api.variant.LibraryVariantBuilder
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import com.frybits.gradle.android.configurations.app.androidAppConfiguration
import com.frybits.gradle.android.configurations.app.androidAppVariantBuilderConfiguration
import com.frybits.gradle.android.configurations.app.androidAppVariantConfiguration
import com.frybits.gradle.android.configurations.common.androidBaseConfiguration
import com.frybits.gradle.android.configurations.common.androidCommonConfiguration
import com.frybits.gradle.android.configurations.common.androidPlugins
import com.frybits.gradle.android.configurations.common.androidVariantBuilderConfiguration
import com.frybits.gradle.android.configurations.common.androidVariantConfiguration
import com.frybits.gradle.android.configurations.library.androidLibraryConfiguration
import com.frybits.gradle.android.configurations.library.androidLibraryVariantBuilderConfiguration
import com.frybits.gradle.android.configurations.library.androidLibraryVariantConfiguration
import com.frybits.gradle.android.wrappers.AGP9ComponentsExtensionWrapper
import com.frybits.gradle.core.Configurer
import com.frybits.gradle.core.definitions.AndroidBuildFile
import com.frybits.gradle.core.definitions.BuildFile
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

/**
 * Performs the configuration of the android and androidComponents objects
 */
public abstract class AGP9Configurer @Inject internal constructor(
    private val project: Project,
    private val componentsExtension: AndroidComponentsExtension<CommonExtension, VariantBuilder, Variant>
): Configurer {

    override fun applyPlugins(buildFile: BuildFile) {
        require(buildFile is AndroidBuildFile) { "Attempting to configure ${buildFile::class} with Android configurations" }
        with(project) {
            androidPlugins(buildFile)
        }
    }

    override fun configureBuild(buildFile: BuildFile) {
        require(buildFile is AndroidBuildFile) { "Attempting to configure ${buildFile::class} with Android configurations" }
        with(project) {
            androidBaseConfiguration(buildFile)

            objects.newInstance<AGP9ComponentsExtensionWrapper>(componentsExtension).run {
                beforeVariants { variantBuilder ->
                    androidVariantBuilderConfiguration(buildFile, variantBuilder)
                    when(variantBuilder) {
                        is ApplicationVariantBuilder -> androidAppVariantBuilderConfiguration(buildFile, variantBuilder)
                        is LibraryVariantBuilder -> androidLibraryVariantBuilderConfiguration(buildFile, variantBuilder)
                        else -> throw GradleException("Unsupported type ${variantBuilder::class}")
                    }
                }

                onVariants { variant ->
                    androidVariantConfiguration(buildFile, variant)
                    when (variant) {
                        is ApplicationVariant -> androidAppVariantConfiguration(buildFile, variant)
                        is LibraryVariant -> androidLibraryVariantConfiguration(buildFile, variant)
                        else -> throw GradleException("Unsupported type ${variant::class}")
                    }
                }

                finalizeDsl { commonExtensionWrapper ->
                    androidCommonConfiguration(buildFile, commonExtensionWrapper)

                    when(val androidExtension = commonExtensionWrapper.commonExtension) {
                        is ApplicationExtension -> androidAppConfiguration(buildFile, androidExtension)
                        is LibraryExtension -> androidLibraryConfiguration(buildFile, androidExtension)
                        else -> throw GradleException("Unsupported type ${androidExtension::class}")
                    }
                }
            }
        }
    }
}

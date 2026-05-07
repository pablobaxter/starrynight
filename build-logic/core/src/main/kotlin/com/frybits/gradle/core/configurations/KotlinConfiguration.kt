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

package com.frybits.gradle.core.configurations

import com.frybits.gradle.utils.kotlinJvmTarget
import dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi
import dev.zacsweers.metro.gradle.MetroPluginExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * Configures all Kotlin projects
 */
public fun Project.kotlinProjectConfiguration() {
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "com.google.devtools.ksp")
    configureMetro()

    extensions.configure<HasConfigurableKotlinCompilerOptions<KotlinJvmCompilerOptions>>("kotlin") {
        compilerOptions {
            jvmTarget.set(kotlinJvmTarget)
            allWarningsAsErrors.set(true)
        }
    }

    kotlinExtension.run {
        explicitApi()
    }
}

@OptIn(ExperimentalMetroGradleApi::class)
private fun Project.configureMetro() {
    apply(plugin = "dev.zacsweers.metro")
    configure<MetroPluginExtension> {
        generateContributionProviders.set(true)
    }
}

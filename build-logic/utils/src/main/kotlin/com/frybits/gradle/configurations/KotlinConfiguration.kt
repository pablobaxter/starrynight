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

package com.frybits.gradle.configurations

import com.frybits.gradle.utils.kotlinJvmTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * Configures all Kotlin projects
 */
fun Project.kotlinProjectConfiguration() {
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "com.google.devtools.ksp")

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

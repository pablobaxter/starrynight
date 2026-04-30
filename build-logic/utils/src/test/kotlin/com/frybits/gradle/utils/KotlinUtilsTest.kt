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

import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KotlinUtilsTest {

    @TempDir
    lateinit var rootDir: Path

    @Test
    fun `kotlinJvmTarget provider parses correctly`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.kotlin.jvm.target=15")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        val result = project.kotlinJvmTarget.get()
        assertEquals(JvmTarget.JVM_15, result)
    }

    @Test
    fun `kotlinJvmTarget provider fails with malformed property`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.kotlin.jvm.target=fifteen")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        assertFailsWith<IllegalArgumentException> {
            project.kotlinJvmTarget.get()
        }
    }
}

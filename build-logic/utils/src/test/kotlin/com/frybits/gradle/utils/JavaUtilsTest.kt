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
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaUtilsTest {

    @TempDir
    lateinit var rootDir: Path

    @Test
    fun `targetCompatibility provider parses correctly`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.java.compatibility.target=15")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        val result = project.javaTargetCompatibility.get()
        assertEquals(JavaVersion.VERSION_15, result)
    }

    @Test
    fun `targetCompatibility provider fails with malformed property`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.java.compatibility.target=fifteen")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        assertFailsWith<IllegalArgumentException> {
            project.javaTargetCompatibility.get()
        }
    }

    @Test
    fun `sourceCompatibility provider parses correctly`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.java.compatibility.source=15")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        val result = project.javaSourceCompatibility.get()
        assertEquals(JavaVersion.VERSION_15, result)
    }

    @Test
    fun `sourceCompatibility provider fails with malformed property`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.java.compatibility.source=fifteen")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        assertFailsWith<IllegalArgumentException> {
            project.javaSourceCompatibility.get()
        }
    }

    @Test
    fun `android targetCompatibility provider parses correctly`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.android.compatibility.target=15")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        val result = project.androidTargetCompatibility.get()
        assertEquals(JavaVersion.VERSION_15, result)
    }

    @Test
    fun `android targetCompatibility provider fails with malformed property`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.android.compatibility.target=fifteen")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        assertFailsWith<IllegalArgumentException> {
            project.androidTargetCompatibility.get()
        }
    }

    @Test
    fun `android sourceCompatibility provider parses correctly`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.android.compatibility.source=15")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        val result = project.androidSourceCompatibility.get()
        assertEquals(JavaVersion.VERSION_15, result)
    }

    @Test
    fun `android sourceCompatibility provider fails with malformed property`() {
        rootDir.resolve("gradle.properties").writeText("com.frybits.android.compatibility.source=fifteen")
        val project = ProjectBuilder.builder()
            .withProjectDir(rootDir.toFile())
            .build()

        assertFailsWith<IllegalArgumentException> {
            project.androidSourceCompatibility.get()
        }
    }
}

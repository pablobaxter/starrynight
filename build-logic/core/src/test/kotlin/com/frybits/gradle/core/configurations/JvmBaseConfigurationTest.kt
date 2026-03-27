package com.frybits.gradle.core.configurations

import org.gradle.api.JavaVersion
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmBaseConfigurationTest {

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `target and source compatibilities are set`() {
        projectDir.resolve("gradle.properties").writeText("""
            com.frybits.java.compatibility.target=15
            com.frybits.java.compatibility.source=17
        """.trimIndent())
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir.toFile())
            .build()

        project.apply<JavaPlugin>()

        project.jvmProjectConfiguration()

        val extension = project.the<JavaPluginExtension>()
        assertEquals(JavaVersion.VERSION_15, extension.targetCompatibility)
        assertEquals(JavaVersion.VERSION_17, extension.sourceCompatibility)
    }
}

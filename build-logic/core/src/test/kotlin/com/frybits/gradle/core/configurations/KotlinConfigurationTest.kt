package com.frybits.gradle.core.configurations

import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByName
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinConfigurationTest {

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `should apply serialization plugin`() {
        val project = ProjectBuilder.builder().build()
        project.apply(plugin = "org.jetbrains.kotlin.jvm")

        project.kotlinProjectConfiguration()

        assertTrue(project.plugins.hasPlugin("kotlinx-serialization"))
    }

    @Test
    fun `should apply ksp plugin`() {
        val project = ProjectBuilder.builder().build()
        project.apply(plugin = "org.jetbrains.kotlin.jvm")

        project.kotlinProjectConfiguration()

        assertTrue(project.plugins.hasPlugin("com.google.devtools.ksp"))
    }

    @Test
    fun `configures kotlin extension`() {
        projectDir.resolve("gradle.properties").writeText("com.frybits.kotlin.jvm.target=17")
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir.toFile())
            .build()
        project.apply(plugin = "org.jetbrains.kotlin.jvm")

        project.kotlinProjectConfiguration()

        val extension = project.extensions.getByName<HasConfigurableKotlinCompilerOptions<KotlinJvmCompilerOptions>>("kotlin")

        assertEquals(JvmTarget.JVM_17, extension.compilerOptions.jvmTarget.get())
        assertTrue(extension.compilerOptions.allWarningsAsErrors.get())
    }

    @Test
    fun `configures explicitApi`() {
        val project = ProjectBuilder.builder().build()
        project.apply(plugin = "org.jetbrains.kotlin.jvm")

        project.kotlinProjectConfiguration()

        val extension = project.kotlinExtension

        assertEquals(ExplicitApiMode.Strict, extension.explicitApi)
    }
}

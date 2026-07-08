package com.frybits.gradle.core.configurations

import com.frybits.gradle.core.definitions.JavaLibraryBuildFile
import com.frybits.gradle.core.utils.DummyPlugin
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.add
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposeConfigurationTest {

    @Test
    fun `BuildFile with compose flag adds compose plugin`() {
        val buildFile = JavaLibraryBuildFile(enableCompose = true)
        val project = ProjectBuilder.builder().build()

        project.handleComposePlugins(buildFile)

        assertTrue(project.plugins.hasPlugin("org.jetbrains.kotlin.plugin.compose"))
        assertEquals(DummyPlugin::class, project.plugins.getPlugin("org.jetbrains.kotlin.plugin.compose")::class)
    }

    @Test
    fun `BuildFile without compose flag omits compose plugin`() {
        val buildFile = JavaLibraryBuildFile(enableCompose = false)
        val project = ProjectBuilder.builder().build()

        project.handleComposeConfiguration(buildFile)

        assertFalse(project.plugins.hasPlugin("org.jetbrains.kotlin.plugin.compose"))
    }

    @Test
    fun `BuildFile with compose flag adds compose plugin via baseProjectConfiguration`() {
        val buildFile = JavaLibraryBuildFile(enableCompose = true)
        val project = ProjectBuilder.builder().build()
        val versionCatalog = DummyVersionCatalog(project)
        project.extensions.add<VersionCatalogsExtension>("libs", DummyVersionCatalogExtension(versionCatalog))

        project.baseProjectPlugins(buildFile)

        assertTrue(project.plugins.hasPlugin("org.jetbrains.kotlin.plugin.compose"))
        assertEquals(DummyPlugin::class, project.plugins.getPlugin("org.jetbrains.kotlin.plugin.compose")::class)
    }
}

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

import com.frybits.gradle.core.definitions.JavaLibraryBuildFile
import com.frybits.gradle.core.definitions.Library
import com.frybits.gradle.core.definitions.Platform
import com.frybits.gradle.core.definitions.Project as FrybitsProject
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.capabilities.Capability
import org.gradle.api.internal.artifacts.dependencies.AbstractExternalModuleDependency
import org.gradle.api.internal.artifacts.dependencies.MinimalExternalModuleDependencyInternal
import org.gradle.api.internal.attributes.AttributesFactory
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.internal.typeconversion.NotationParser
import org.gradle.kotlin.dsl.add
import org.gradle.plugin.use.PluginDependency
import org.gradle.testfixtures.ProjectBuilder
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BaseProjectConfigurationTest {

    @Test
    fun `BuildFile with empty dependencies does not populate configurations`() {
        val buildFile = JavaLibraryBuildFile()
        val project = ProjectBuilder.builder().build()

        project.baseProjectConfiguration(buildFile)

        assertTrue(project.configurations.isEmpty())
    }

    @Test
    fun `BuildFile with external dependencies populates configurations`() {
        val buildFile = JavaLibraryBuildFile(
            dependencies = mapOf("implementation" to listOf(Library("com.frybits:module:1.2.3")))
        )
        val project = ProjectBuilder.builder().build()
        val versionCatalog = DummyVersionCatalog(project)
        project.extensions.add<VersionCatalogsExtension>("libs", DummyVersionCatalogExtension(versionCatalog))
        val configuration = project.configurations.register("implementation")

        project.baseProjectConfiguration(buildFile)
        (project as DefaultProject).evaluate()

        assertContains(configuration.get().dependencies.map { it.toString() }, "com.frybits:module:1.2.3")
        assertTrue(configuration.get().dependencies.filterIsInstance<ModuleDependency>().all { it.attributes.isEmpty })
    }

    @Test
    fun `BuildFile with platform external dependencies populates configurations`() {
        val buildFile = JavaLibraryBuildFile(
            dependencies = mapOf("implementation" to listOf(Platform(Library("com.frybits:module:1.2.3"))))
        )
        val project = ProjectBuilder.builder().build()
        val versionCatalog = DummyVersionCatalog(project)
        project.extensions.add<VersionCatalogsExtension>("libs", DummyVersionCatalogExtension(versionCatalog))
        val configuration = project.configurations.register("implementation")

        project.baseProjectConfiguration(buildFile)
        (project as DefaultProject).evaluate()

        assertContains(configuration.get().dependencies.map { it.toString() }, "com.frybits:module:1.2.3")
        assertContains(configuration.get().dependencies.filterIsInstance<ModuleDependency>().map { it.attributes.toString() }, "{org.gradle.category=platform}")
    }

    @Test
    fun `BuildFile with project dependencies populates configurations`() {
        val buildFile = JavaLibraryBuildFile(
            dependencies = mapOf("implementation" to listOf(FrybitsProject(":foobar")))
        )
        val project = ProjectBuilder.builder().build()
        ProjectBuilder.builder()
            .withName("foobar")
            .withParent(project)
            .build()
        val versionCatalog = DummyVersionCatalog(project)
        project.extensions.add<VersionCatalogsExtension>("libs", DummyVersionCatalogExtension(versionCatalog))
        val configuration = project.configurations.register("implementation")

        project.baseProjectConfiguration(buildFile)
        (project as DefaultProject).evaluate()

        assertContains(configuration.get().dependencies.map { it.toString() }, "project ':foobar'")
        assertTrue(configuration.get().dependencies.filterIsInstance<ModuleDependency>().all { it.attributes.isEmpty })
    }

    @Test
    fun `BuildFile with platform project dependencies populates configurations`() {
        val buildFile = JavaLibraryBuildFile(
            dependencies = mapOf("implementation" to listOf(Platform(FrybitsProject(":foobar"))))
        )
        val project = ProjectBuilder.builder().build()
        ProjectBuilder.builder()
            .withName("foobar")
            .withParent(project)
            .build()
        val versionCatalog = DummyVersionCatalog(project)
        project.extensions.add<VersionCatalogsExtension>("libs", DummyVersionCatalogExtension(versionCatalog))
        val configuration = project.configurations.register("implementation")

        project.baseProjectConfiguration(buildFile)
        (project as DefaultProject).evaluate()

        assertContains(configuration.get().dependencies.map { it.toString() }, "project ':foobar'")
        assertContains(configuration.get().dependencies.filterIsInstance<ModuleDependency>().map { it.attributes.toString() }, "{org.gradle.category=platform}")
    }

    @Test
    fun `BuildFile with multiple dependencies populates configurations`() {
        val buildFile = JavaLibraryBuildFile(
            dependencies = mapOf("implementation" to listOf(
                FrybitsProject(":foobar"),
                Library("com.frybits:module:1.2.3"),
                Platform(Library("com.frybits:other:3.4"))
            ))
        )
        val project = ProjectBuilder.builder().build()
        ProjectBuilder.builder()
            .withName("foobar")
            .withParent(project)
            .build()
        val versionCatalog = DummyVersionCatalog(project)
        project.extensions.add<VersionCatalogsExtension>("libs", DummyVersionCatalogExtension(versionCatalog))
        val configuration = project.configurations.register("implementation")

        project.baseProjectConfiguration(buildFile)
        (project as DefaultProject).evaluate()

        val dependencies = configuration.get().dependencies.map { it.toString() }.toSet()

        assertContains(dependencies, "project ':foobar'")
        assertContains(dependencies, "com.frybits:module:1.2.3")
        assertContains(dependencies, "com.frybits:other:3.4")
        assertContains(configuration.get().dependencies.filterIsInstance<ModuleDependency>().filter { it.toString() == "com.frybits:other:3.4" }.map { it.attributes.toString() }, "{org.gradle.category=platform}")
    }

    @Test
    fun `BuildFile fails with missing configuration`() {
        val buildFile = JavaLibraryBuildFile(
            dependencies = mapOf("implementation" to listOf(Library("com.frybits:module:1.2.3")))
        )
        val project = ProjectBuilder.builder().build()
        val versionCatalog = DummyVersionCatalog(project)
        project.extensions.add<VersionCatalogsExtension>("libs", DummyVersionCatalogExtension(versionCatalog))

        project.baseProjectConfiguration(buildFile)
        assertFailsWith<UnknownConfigurationException> {
            try {
                (project as DefaultProject).evaluate()
            } catch (e: ProjectConfigurationException) {
                throw e.cause ?: Exception("No cause for exception")
            }
        }
    }

    @Test
    fun `BuildFile fails with missing version catalog`() {
        val buildFile = JavaLibraryBuildFile(
            dependencies = mapOf("implementation" to listOf(Library("com.frybits:module:1.2.3")))
        )
        val project = ProjectBuilder.builder().build()

        project.baseProjectConfiguration(buildFile)
        assertFailsWith<InvalidUserDataException> {
            try {
                (project as DefaultProject).evaluate()
            } catch (e: ProjectConfigurationException) {
                throw e.cause ?: Exception("No cause for exception")
            }
        }
    }
}

private class DummyVersionCatalog(private val project: Project) : VersionCatalog {

    override fun findLibrary(alias: String): Optional<Provider<MinimalExternalModuleDependency>> {
        val dependency = DummyDependency(project.dependencies.create(alias) as ExternalModuleDependency)
        return Optional.of(project.providers.provider { dependency })
    }

    override fun findBundle(alias: String): Optional<Provider<ExternalModuleDependencyBundle>> = Optional.empty()

    override fun findVersion(alias: String): Optional<VersionConstraint> = Optional.empty()

    override fun findPlugin(alias: String): Optional<Provider<PluginDependency>> = Optional.empty()

    override fun getLibraryAliases(): List<String> = emptyList()

    override fun getBundleAliases(): List<String> = emptyList()

    override fun getVersionAliases(): List<String> = emptyList()

    override fun getPluginAliases(): List<String> = emptyList()

    override fun getName(): String = "dummy"
}

private class DummyDependency(private val dependency: ExternalModuleDependency): ExternalModuleDependency by dependency, MinimalExternalModuleDependencyInternal {

    override fun copy(): MinimalExternalModuleDependency {
        return this
    }

    override fun copyTo(target: AbstractExternalModuleDependency) {}

    override fun getAttributesFactory(): AttributesFactory {
        TODO()
    }

    override fun getCapabilityNotationParser(): NotationParser<Any, Capability> {
        TODO()
    }

    override fun getObjectFactory(): ObjectFactory {
        TODO()
    }
}

private class DummyVersionCatalogExtension(private val versionCatalog: VersionCatalog): VersionCatalogsExtension {

    override fun find(name: String): Optional<VersionCatalog> {
        return Optional.of(versionCatalog)
    }

    override fun named(name: String): VersionCatalog {
        return versionCatalog
    }

    override fun getCatalogNames(): Set<String> {
        return setOf("libs")
    }

    override fun iterator(): MutableIterator<VersionCatalog> {
        return arrayListOf(versionCatalog).iterator()
    }
}

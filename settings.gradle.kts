pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
        id("com.android.settings") version providers.gradleProperty("com.frybits.agp.version")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
    id("com.android.settings")
}

includeBuild("build-logic")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        register("libs") {
            version("agp-version", providers.gradleProperty("com.frybits.agp.version").get())
            version("kotlin-version", providers.gradleProperty("com.frybits.kotlin.version").get())
            version("ksp-version", providers.gradleProperty("com.frybits.ksp.version").get())
        }
    }
}

@Suppress("UnstableApiUsage")
val projectList: Provider<Array<String>> = providers.fileContents(layout.rootDirectory.dir("gradle").file("all-projects.txt"))
    .asText
    .map { text ->
        return@map text.lines().toTypedArray()
    }
include(*projectList.get())

rootProject.name = providers.gradleProperty("com.frybits.name").get()

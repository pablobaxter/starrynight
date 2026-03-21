pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
        id("com.android.settings") version providers.gradleProperty("com.frybits.agp.version")

        id("com.android.application") version providers.gradleProperty("com.frybits.agp.version")
        id("com.android.library") version providers.gradleProperty("com.frybits.agp.version")
        id("org.jetbrains.kotlin.android") version providers.gradleProperty("com.frybits.kotlin.version")
        id("org.jetbrains.kotlin.jvm") version providers.gradleProperty("com.frybits.kotlin.version")
        id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
        id("com.google.devtools.ksp") version providers.gradleProperty("com.frybits.ksp.version")
        id("org.gradle.android.cache-fix") version "3.0.3"
    }

    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
    id("com.android.settings")
    id("com.frybits.plugin") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id("com.google.devtools.ksp") apply false
    id("org.gradle.android.cache-fix") apply false
}

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

@Suppress("UnstableApiUsage")
gradle.lifecycle.beforeProject {
    apply(plugin = "com.frybits.plugin")
}

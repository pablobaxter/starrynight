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
        id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0-RC"
        id("com.google.devtools.ksp") version providers.gradleProperty("com.frybits.ksp.version")
        id("org.gradle.android.cache-fix") version "3.0.3"
        id("dev.zacsweers.metro") version providers.gradleProperty("com.frybits.metro.version")
        id("org.jetbrains.kotlin.plugin.compose") version providers.gradleProperty("com.frybits.kotlin.version")
        id("androidx.room") version "2.8.4"
        id("com.squareup.wire") version "6.4.0"
    }

    includeBuild("build-logic")

    buildscript {
        val r8Version = providers.gradleProperty("com.frybits.r8.version")
        repositories {
            mavenCentral()
            if (r8Version.isPresent) {
                maven {
                    url = uri("https://storage.googleapis.com/r8-releases/raw")
                }
            }
        }
        dependencies {
            if (r8Version.isPresent) {
                classpath(r8Version.map { "com.android.tools:r8:$it" })
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
    id("com.android.settings")
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id("com.google.devtools.ksp") apply false
    id("org.gradle.android.cache-fix") apply false
    id("dev.zacsweers.metro") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false
    id("androidx.room") apply false
    id("com.squareup.wire") apply false
    id("com.frybits.plugin")
}

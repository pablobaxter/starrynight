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
    }

    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
    id("com.android.settings")
    id("com.frybits.settings")
}

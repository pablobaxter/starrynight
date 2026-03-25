pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    versionCatalogs {
        create("logic") {
            from(files("logic.versions.toml"))
        }
    }
}

include(":atproto")
include(":android")
include(":android:agp8")
include(":android:agp9")
include(":core")
include(":plugins")
include(":utils")

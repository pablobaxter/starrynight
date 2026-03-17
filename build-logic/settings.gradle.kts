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
//            version("agp-version", )
        }
    }
}

include(":plugins")
include(":utils")

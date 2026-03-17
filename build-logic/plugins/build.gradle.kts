plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `jvm-test-suite`
    kotlin("plugin.serialization") version "2.3.10"
}

testing {
    @Suppress("UnstableApiUsage", "unused")
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest()
        }

        val functionalTest by registering(JvmTestSuite::class) {
            useKotlinTest()
        }
    }
}

// Special thanks to https://github.com/melix/jmh-gradle-plugin/blob/master/build-logic/src/main/kotlin/me.champeau.convention-funcTest.gradle.kts
val pluginsUnderTest by configurations.registering {
    isCanBeConsumed = false
    isCanBeResolved = false
}

gradlePlugin {
    plugins {
        // Base plugin
        register("frybitsPlugin") {
            id = "com.frybits.plugin"
            implementationClass = "com.frybits.gradle.plugins.FrybitsPlugin"
        }
    }

    // These functional tests are performing actual Gradle builds, and aren't automatically added by the testing suite. Adding here instead.
    @Suppress("UnstableApiUsage")
    testSourceSet(sourceSets.getByName("functionalTest"))
}

dependencies {
    implementation(project(":utils"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.agp)

    implementation(logic.kotlinx.serialization)
}

tasks.withType<ValidatePlugins>().configureEach {
    enableStricterValidation.set(true)
}

// Checks should include all tests
tasks.check {
    dependsOn("functionalTest")
}

// Add in the classpaths the functional tests will expect
tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(pluginsUnderTest)
}

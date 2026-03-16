plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `jvm-test-suite`
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
        // Root project plugin
        register("frybitsSettingPlugin") {
            id = "com.frybits.settings"
            implementationClass = "com.frybits.gradle.plugins.FrybitsSettingsPlugin"
        }
    }

    // These functional tests are performing actual Gradle builds, and aren't automatically added by the testing suite. Adding here instead.
    @Suppress("UnstableApiUsage")
    testSourceSet(sourceSets.getByName("functionalTest"))
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
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

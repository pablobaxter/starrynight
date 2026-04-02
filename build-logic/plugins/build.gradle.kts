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
val pluginsUnderTest by configurations.registering

gradlePlugin {
    plugins {
        // Base plugin
        register("frybitsPlugin") {
            id = "com.frybits.plugin"
            implementationClass = "com.frybits.gradle.plugins.FrybitsPlugin"
        }
    }

    // These functional tests are performing actual Gradle builds, and aren't automatically added by the testing suite. Adding here instead.
    testSourceSets(sourceSets.getByName("functionalTest"))
}

dependencies {
    implementation(project(":atproto"))
    implementation(project(":android"))
    implementation(project(":android:agp8"))
    implementation(project(":android:agp9"))
    implementation(project(":core"))
    implementation(project(":utils"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.agp.zip(providers.gradleProperty("agp")) { lib, version -> "$lib:$version" })
    compileOnly(logic.kotlin.gradle)
    compileOnly(logic.ksp)
    compileOnly(logic.r8)

    implementation(logic.kotlinx.serialization.core)
    implementation(logic.bundles.kotlinx.toml)
}

kotlin {
    explicitApi()
}

tasks.withType<ValidatePlugins>().configureEach {
    enableStricterValidation.set(true)
}

// Add in the classpaths the functional tests will expect
tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(pluginsUnderTest)
}

// Checks should include all tests
tasks.check {
    dependsOn("functionalTest")
}

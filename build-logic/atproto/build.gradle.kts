plugins {
    `kotlin-dsl`
    `jvm-test-suite`
    kotlin("plugin.serialization") version "2.3.10"
}

testing {
    @Suppress("UnstableApiUsage", "unused")
    suites {
        getByName<JvmTestSuite>("test") {
            useKotlinTest()
        }
    }
}

dependencies {
    implementation(project(":core"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.kotlin.gradle)

    implementation(logic.kotlin.poet)
    implementation(logic.kotlinx.serialization.core)
    implementation(logic.kotlinx.serialization.json)
}

kotlin {
    explicitApi()
}

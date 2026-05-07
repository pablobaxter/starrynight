plugins {
    `kotlin-dsl`
    `jvm-test-suite`
    kotlin("plugin.serialization") version "2.3.10"
}

testing {
    @Suppress("UnstableApiUsage", "unused")
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest()
        }
    }
}

dependencies {
    implementation(project(":utils"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.kotlin.gradle)

    compileOnly(logic.metro)

    implementation(logic.kotlinx.serialization.core)

    testImplementation(logic.kotlin.gradle)
}

kotlin {
    explicitApi()
}

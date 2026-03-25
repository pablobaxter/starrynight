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
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    implementation(logic.kotlinx.serialization.core)
    implementation(logic.kotlinx.serialization.json)
}

kotlin {
    explicitApi()
}

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

            dependencies {
                implementation(logic.kotlin.gradle)
            }
        }
    }
}

dependencies {
    implementation(project(":utils"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.kotlin.gradle)

    compileOnly(logic.metro)
    compileOnly(logic.protobuf)
    compileOnly(logic.room)

    implementation(logic.kotlinx.serialization.core)
}

kotlin {
    explicitApi()
}

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

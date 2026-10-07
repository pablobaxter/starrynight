plugins {
    `kotlin-dsl`
    `jvm-test-suite`
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
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.kotlin.gradle)
}

kotlin {
    explicitApi()
}

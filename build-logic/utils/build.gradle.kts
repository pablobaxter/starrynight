plugins {
    `kotlin-dsl`
    `jvm-test-suite`
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
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.kotlin.gradle)
}

kotlin {
    explicitApi()
}

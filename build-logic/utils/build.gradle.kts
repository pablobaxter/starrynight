plugins {
    `kotlin-dsl`
    `jvm-test-suite`
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

    compileOnly(logic.kotlin.gradle)

    testImplementation(logic.kotlin.gradle)
}

kotlin {
    explicitApi()
}

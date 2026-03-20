plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.3.10"
}

dependencies {
    implementation(project(":utils"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.kotlin.gradle)

    implementation(logic.kotlinx.serialization.core)
}

kotlin {
    explicitApi()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.kotlin.gradle)
}

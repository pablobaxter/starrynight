plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":android"))
    implementation(project(":core"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.agp.map { "$it:9.3.0-alpha10" })
}

kotlin {
    explicitApi()
}

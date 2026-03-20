plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":android"))
    implementation(project(":core"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.agp.map { "$it:8.13.2" })
}

kotlin {
    explicitApi()
}

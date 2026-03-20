plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":android"))
    implementation(project(":core"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.agp.map { "$it:9.2.0-alpha04" })
}

kotlin {
    explicitApi()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    implementation(project(":android"))
    implementation(project(":utils"))

    compileOnly(logic.agp.map { "$it:8.13.2" })
}

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    implementation(project(":android"))
    implementation(project(":utils"))

    compileOnly(logic.agp.map { "$it:9.2.0-alpha04" })
}

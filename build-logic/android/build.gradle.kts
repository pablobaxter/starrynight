plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.agp.zip(providers.gradleProperty("agp")) { lib, version -> "$lib:$version" })

    implementation(project(":utils"))
}

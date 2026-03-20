plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":core"))
    implementation(project(":utils"))

    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    compileOnly(logic.agp.zip(providers.gradleProperty("agp")) { lib, version -> "$lib:$version" })
}

kotlin {
    explicitApi()
}

buildscript {
    dependencies {
        val r8Version = providers.gradleProperty("com.frybits.r8.version")
        if (r8Version.isPresent) {
            classpath(r8Version.map { "com.android.tools:r8:$it" })
        }
    }
}

plugins {
    id("com.frybits.plugin")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.cache.fix) apply false
}

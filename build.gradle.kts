// This build file should not be modified
buildscript {
    dependencies {
        val r8Version = providers.gradleProperty("com.frybits.r8.version")
        if (r8Version.isPresent) {
            classpath(r8Version.map { "com.android.tools:r8:$it" })
        }
    }
}
// End build file

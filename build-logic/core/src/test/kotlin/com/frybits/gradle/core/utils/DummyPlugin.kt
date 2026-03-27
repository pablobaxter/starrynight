package com.frybits.gradle.core.utils

import org.gradle.api.Plugin
import org.gradle.api.Project

internal class DummyPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        println("Applied dummy plugin")
    }
}

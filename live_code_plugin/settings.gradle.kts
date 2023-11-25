pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm").version("1.9.20")
        id("org.jetbrains.compose").version("1.5.10")
    }
}

rootProject.name = "live_code_plugin"
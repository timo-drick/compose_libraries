import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.4.0-alpha01-dev1004"//"1.3.1"
}

group = "de.appsonair.compose.sksl"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.common)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
}

kotlin {
    jvmToolchain(11)
}

compose {
    kotlinCompilerPlugin.set("androidx.compose.compiler:compiler:1.4.4")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "de.appsonair.compose.sksl"
            packageVersion = "1.0.0"
        }
    }
}


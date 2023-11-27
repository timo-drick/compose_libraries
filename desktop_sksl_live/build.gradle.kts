import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version Versions.composeDesktop
}

group = "de.appsonair.compose.sksl"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.common)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    val ktorVersion = "2.3.4"
    implementation("io.ktor:ktor-network:$ktorVersion")
}

kotlin {
    jvmToolchain(11)
}

compose {
    kotlinCompilerPlugin.set("androidx.compose.compiler:compiler:${Versions.composeCompiler}")
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


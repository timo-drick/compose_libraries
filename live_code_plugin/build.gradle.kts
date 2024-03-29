plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.20"
  id("org.jetbrains.intellij") version "1.15.0"
  id("org.jetbrains.compose")
  id("idea")
}

group = "de.appsonair.compose"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  google()
  maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
  implementation(compose.desktop.currentOs)
  val ktorVersion = "2.3.4"
  implementation("io.ktor:ktor-network:$ktorVersion")
}


// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2022.3")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf("android"))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  runIde {
    ideDir.set(file("/home/timo/devutils/android-studio"))
  }

  patchPluginXml {
    sinceBuild.set("223")   // intellij 2022.3
    untilBuild.set("233.*") // intellij 2023.3

    pluginDescription.set("""
      Monitors and transfers file changes for connected Android clients that want to know when a file
      on the developer machine is changed. </br>
      This is used to do live coding for AGSL shaders. </br>
      Please note that currently only text files are supported. </br>
      Project url: https://github.com/timo-drick/compose_libraries/tree/main/live_code_plugin
    """.trimIndent())
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}

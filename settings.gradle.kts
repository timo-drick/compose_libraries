rootProject.name = "Compose libraries"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
include(":app")
include(":opengl_pixel_shader")
include(":smooth_progress_indication")
include(":common")
include(":kdatastore")
include(":desktop_sksl_live")

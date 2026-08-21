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
        // osmdroid + some transitive deps are published here
        maven("https://jitpack.io")
    }
}

rootProject.name = "DTTrackPro"
include(":app")

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
    }
}

rootProject.name = "Hibari2"
include(":androidApp")
include(":shared")
include(":shared:runtime")
include(":shared:foundation")
include(":shared:animation:animation-core")
include(":shared:animation:animation")
include(":shared:components:material")
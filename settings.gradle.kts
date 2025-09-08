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
include(":shared:core")
include(":shared:foundation")
include(":shared:animation:animation-core")
include(":shared:animation:animation")
include(":shared:components:material")
//include(":shared:compose-runtime:runtime")
//include(":shared:compose-runtime:runtime-annotation")
//include(":shared:compose-runtime:runtime-saveable")
//include(":shared:ui:ui")
//include(":shared:ui:ui-android-stubs")
//include(":shared:ui:ui-text")
//include(":shared:ui:ui-geometry")
//include(":shared:ui:ui-graphics")
//include(":shared:ui:ui-util")
//include(":shared:ui:ui-unit")
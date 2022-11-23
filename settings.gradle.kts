pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "NavigationComposeScreen"
include(":app")
include(":annotation")
include(":common")
include(":navigation")
include(":compiler")
include(":library")
include(":compile-testing")

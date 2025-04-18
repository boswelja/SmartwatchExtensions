// Foundational modules
include(
    ":core:common",
    ":core:mobile",
    ":core:wear",
    ":play-services-wearable-ext"
)

// Feature modules
include(
    ":appmanager:common",
    ":appmanager:mobile",
    ":appmanager:wear",
    ":batterysync:common",
    ":batterysync:mobile",
    ":batterysync:wear",
    ":dndsync:common",
    ":dndsync:mobile",
    ":dndsync:wear",
    ":phonelocking:common",
    ":phonelocking:mobile",
    ":phonelocking:wear",
    ":watchmanager:common",
    ":watchmanager:mobile",
    ":watchmanager:wear",
)

// App modules
include(
    ":mobile",
    ":wearos"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("com.android.settings") version "8.9.1"
}

android {
    targetSdk = 34
    compileSdk = 36
    minSdk = 26
}
include(
    ":appmanager:common",
    ":appmanager:mobile",
    ":appmanager:wear",
    ":batterysync:common",
    ":batterysync:mobile",
    ":batterysync:wear",
    ":devicemanagement:common",
    ":devicemanagement:mobile",
    ":dndsync:common",
    ":dndsync:wear",
    ":messages:common",
    ":messages:mobile",
    ":phonelocking",
    ":settings:common",
    ":settings:mobile",
    ":mobile",
    ":wearos"
)
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.gms.google-services" ->
                    useModule("com.google.gms:google-services:${requested.version}")
                // Wire can be removed with 4.0.0
                "com.squareup.wire" ->
                    useModule("com.squareup.wire:wire-gradle-plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

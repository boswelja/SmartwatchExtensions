includeBuild("plugins")
include(
    ":appmanager:common",
    ":appmanager:mobile",
    ":appmanager:wear",
    ":batterysync:common",
    ":batterysync:mobile",
    ":batterysync:wear",
    ":devicemanagement:common",
    ":devicemanagement:mobile",
    ":devicemanagement:wear",
    ":dndsync:common",
    ":dndsync:mobile",
    ":dndsync:wear",
    ":messages:common",
    ":messages:mobile",
    ":phonelocking:common",
    ":phonelocking:mobile",
    ":phonelocking:wear",
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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

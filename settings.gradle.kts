includeBuild("plugins")
include(
    ":appmanager:common",
    ":appmanager:mobile",
    ":appmanager:wear",
    ":batterysync:common",
    ":batterysync:mobile",
    ":batterysync:wear",
    ":core:common",
    ":core:mobile",
    ":core:wear",
    ":dndsync:common",
    ":dndsync:mobile",
    ":dndsync:wear",
    ":phonelocking:common",
    ":phonelocking:mobile",
    ":phonelocking:wear",
    ":proximity:common",
    ":proximity:mobile",
    ":proximity:wear",
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

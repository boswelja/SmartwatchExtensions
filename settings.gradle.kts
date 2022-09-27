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
    ":watchmanager:common",
    ":watchmanager:mobile",
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

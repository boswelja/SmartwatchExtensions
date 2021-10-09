include(
    ":appmanager",
    ":batterysync",
    ":dndsync",
    ":phonelocking",
    ":settingssync",
    ":versionsync",
    ":watchmanager",
    ":mobile",
    ":wearos",
    ":common"
)
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

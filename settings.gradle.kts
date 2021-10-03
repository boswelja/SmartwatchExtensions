include(
    ":appmanager",
    ":batterysync",
    ":dndsync",
    ":settingssync",
    ":versionsync",
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

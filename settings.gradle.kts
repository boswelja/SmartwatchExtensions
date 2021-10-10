include(
    ":appmanager:common",
    ":appmanager:mobile",
    ":batterysync:common",
    ":batterysync:mobile",
    ":batterysync:wear",
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

include(
    ":appmanager:common",
    ":appmanager:mobile",
    ":appmanager:wear",
    ":batterysync:common",
    ":batterysync:mobile",
    ":batterysync:wear",
    ":dndsync:common",
    ":dndsync:wear",
    ":messages:common",
    ":messages:mobile",
    ":phonelocking",
    ":settings:common",
    ":settings:mobile",
    ":versionsync",
    ":devicemanagement:mobile",
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

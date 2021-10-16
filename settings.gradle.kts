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

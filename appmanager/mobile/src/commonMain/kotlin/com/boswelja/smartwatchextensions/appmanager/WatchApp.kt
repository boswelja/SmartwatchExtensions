package com.boswelja.smartwatchextensions.appmanager

/**
 * A data class containing detailed info for an app on a device.
 * @param watchId The UID of the device the app is installed on.
 * @param packageName The package name of the app.
 * @param label The app label.
 * @param versionName The app version name.
 * @param versionCode The app version code.
 * @param isSystemApp Whether the app is a system app.
 * @param isLaunchable Whether the app has a launchable Activity.
 * @param isEnabled Whether the app is enabled.
 * @param installTime The app install time in milliseconds.
 * @param updateTime The app last update time in milliseconds.
 * @param permissions A list of permissions the app requests. Where possible, these will be human
 * readable strings.
 */
data class WatchAppDetails(
    val watchId: String,
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val isLaunchable: Boolean,
    val isEnabled: Boolean,
    val installTime: Long,
    val updateTime: Long,
    val permissions: List<String>
)

/**
 * A data class containing basic info for an app on a device.
 * @param packageName The package name of the app.
 * @param label The app label.
 * @param versionName The app version name.
 * @param isSystemApp Whether the app is a system app.
 * @param isEnabled Whether the app is enabled.
 */
data class WatchApp(
    val packageName: String,
    val label: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val isEnabled: Boolean
)

/**
 * A data class containing version info for an app on a device.
 * @param packageName The package name of the app.
 * @param updateTime The app last update time in milliseconds.
 */
data class WatchAppVersion(
    val packageName: String,
    val updateTime: Long
)

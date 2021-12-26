package com.boswelja.smartwatchextensions.appmanager

import android.graphics.Bitmap


/**
 * A data class containing detailed info for an app on a device.
 * @param watchId The UID of the device the app is installed on.
 * @param packageName The package name of the app.
 * @param icon The app icon, or null if no icon is available.
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
data class WatchAppDetailsWithIcon(
    val watchId: String,
    val packageName: String,
    val icon: Bitmap?,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val isLaunchable: Boolean,
    val isEnabled: Boolean,
    val installTime: Long,
    val updateTime: Long,
    val permissions: List<String>
) {
    constructor(details: WatchAppDetails, icon: Bitmap?) : this(
        watchId = details.watchId,
        packageName = details.packageName,
        icon = icon,
        label =  details.label,
        versionName = details.versionName,
        versionCode = details.versionCode,
        isSystemApp = details.isSystemApp,
        isLaunchable = details.isLaunchable,
        isEnabled = details.isEnabled,
        installTime = details.installTime,
        updateTime = details.updateTime,
        permissions = details.permissions
    )
}

/**
 * A data class containing basic info for an app on a device.
 * @param packageName The package name of the app.
 * @param icon The app icon, or null if no icon is available.
 * @param label The app label.
 * @param versionName The app version name.
 * @param isSystemApp Whether the app is a system app.
 * @param isEnabled Whether the app is enabled.
 */
data class WatchAppWithIcon(
    val packageName: String,
    val icon: Bitmap?,
    val label: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val isEnabled: Boolean
) {
    constructor(app: WatchApp, icon: Bitmap?) : this(
        packageName = app.packageName,
        icon = icon,
        label = app.label,
        versionName = app.versionName,
        isSystemApp = app.isSystemApp,
        isEnabled = app.isEnabled
    )
}

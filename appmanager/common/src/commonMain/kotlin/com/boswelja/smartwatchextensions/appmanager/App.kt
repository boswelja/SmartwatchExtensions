package com.boswelja.smartwatchextensions.appmanager

import kotlinx.serialization.Serializable

/**
 * Contains information about a package installed on the device.
 * @param versionName The app version name.
 * @param versionCode The app version code.
 * @param packageName The app package.
 * @param label The display name of the app.
 * @param isSystemApp Whether the app is a system app.
 * @param hasLaunchActivity Whether the app is launchable.
 * @param isEnabled Whether the app is enabled.
 * @param installTime The time the app was first installed, in milliseconds since UNIX epoch.
 * @param updateTime The time the app was last updated, in milliseconds since UNIX epoch.
 * @param requestedPermissions The permissions this app requests.
 */
@Serializable
data class App(
    val versionName: String,
    val versionCode: Long,
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val hasLaunchActivity: Boolean,
    val isEnabled: Boolean,
    val installTime: Long,
    val updateTime: Long,
    val requestedPermissions: List<String> = emptyList()
)

/**
 * Contains a list of [App]s.
 * @param apps The list of [App]s.
 */
@Serializable
data class AppList(
    val apps: List<App> = emptyList()
)

/**
 * Contains an app icon for a specified package.
 * @param packageName The package name for the app the icon is for.
 * @param iconBytes The bytes for the app icon bitmap.
 */
@Serializable
data class AppIcon(
    val packageName: String,
    val iconBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AppIcon

        if (packageName != other.packageName) return false
        if (!iconBytes.contentEquals(other.iconBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + iconBytes.contentHashCode()
        return result
    }
}

/**
 * Contains a list of package names for apps that were removed.
 * @param packages THe list of packages that were removed.
 */
@Serializable
data class RemovedApps(
    val packages: List<String> = emptyList()
)

/**
 * Contains information about a version for a single app.
 * @param packageName The package name of the app whose version this is for.
 * @param versionCode The app version code
 */
@Serializable
data class AppVersion(
    val packageName: String,
    val versionCode: Long
)

/**
 * Contains a list of [AppVersion]s.
 * @param versions the list of app versions.
 */
@Serializable
data class AppVersions(
    val versions: List<AppVersion> = emptyList()
)

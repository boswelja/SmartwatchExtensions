package com.boswelja.smartwatchextensions.appmanager.database

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.room.Entity
import com.boswelja.smartwatchextensions.appmanager.App

/**
 * A data class to store information we want to use from [PackageInfo] in a serializable format.
 * @param watchId The ID of the watch this app exists on.
 * @param version The version of the package. Will default to [PackageInfo.versionName], and fall
 * back to [PackageInfoCompat.getLongVersionCode] if we can't get the version name.
 * @param packageName The [PackageInfo.packageName] of the package.
 * @param label The user-facing name for the package. See [PackageManager.getApplicationLabel].
 * @param isSystemApp A boolean to determine whether the package is a system app.
 * @param hasLaunchActivity A boolean to determine whether the package is launchable.
 * @param isEnabled A boolean to indicate whether the app is enabled.
 * @param installTime The time in milliseconds this package was first installed.
 * @param lastUpdateTime The time in milliseconds this package was last updated.
 * @param requestedPermissions An [List] of [android.Manifest.permission]s this package requests.
 */
@Entity(tableName = "watch_apps", primaryKeys = ["watchId", "packageName"])
data class DbApp(
    val watchId: String,
    val version: String,
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val hasLaunchActivity: Boolean,
    val isEnabled: Boolean,
    val installTime: Long,
    val lastUpdateTime: Long,
    val requestedPermissions: List<String>
) {

    constructor(watchId: String, app: App) : this(
        watchId,
        app.version,
        app.packageName,
        app.label,
        app.isSystemApp,
        app.hasLaunchActivity,
        app.isEnabled,
        app.installTime,
        app.lastUpdateTime,
        app.requestedPermissions
    )
}

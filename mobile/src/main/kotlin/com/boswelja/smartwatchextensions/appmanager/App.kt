package com.boswelja.smartwatchextensions.appmanager

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.content.pm.PackageInfoCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.UUID

/**
 * A data class to store information we want to use from [PackageInfo] in a serializable format.
 * @param watchId The ID of the watch this app exists on.
 * @param version The version of the package. Will default to [PackageInfo.versionName], and fall
 * back to [PackageInfoCompat.getLongVersionCode] if we can't get the version name.
 * @param packageName The [PackageInfo.packageName] of the package.
 * @param label The user-facing name for the package. See [PackageManager.getApplicationLabel].
 * @param isSystemApp A boolean to determine whether the package is a system app.
 * @param hasLaunchActivity A boolean to determine whether the package is launchable.
 * @param installTime The time in milliseconds this package was first installed.
 * @param lastUpdateTime The time in milliseconds this package was last updated.
 * @param requestedPermissions An [Array] of [android.Manifest.permission]s this package requests.
 */
@Entity(tableName = "watch_apps", primaryKeys = ["watchId", "packageName"])
data class App(
    val watchId: UUID,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val icon: Bitmap?,
    val version: String,
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val hasLaunchActivity: Boolean,
    val isEnabled: Boolean,
    val installTime: Long,
    val lastUpdateTime: Long,
    val requestedPermissions: Array<String>
) {

    constructor(watchId: UUID, app: com.boswelja.smartwatchextensions.common.appmanager.App) : this(
        watchId,
        app.icon?.bitmap,
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

    override fun toString(): String {
        return label
    }

    override fun equals(other: Any?): Boolean {
        return if (other is App) {
            watchId == other.watchId &&
                packageName == other.packageName &&
                installTime == other.installTime &&
                lastUpdateTime == other.lastUpdateTime
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + watchId.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + isSystemApp.hashCode()
        result = 31 * result + hasLaunchActivity.hashCode()
        result = 31 * result + installTime.hashCode()
        result = 31 * result + lastUpdateTime.hashCode()
        return result
    }
}

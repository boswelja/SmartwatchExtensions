package com.boswelja.smartwatchextensions.appmanager

/**
 * Map an [AppList] to a [List] of [WatchAppDetails].
 * @param sourceUid The UID of the device that sent the [AppList].
 */
fun AppList.mapToWatchAppDetails(sourceUid: String): List<WatchAppDetails> {
    return apps.map {
        WatchAppDetails(
            watchId = sourceUid,
            packageName = it.packageName,
            label = it.label,
            versionName = it.versionName,
            versionCode = it.versionCode,
            isSystemApp = it.isSystemApp,
            isLaunchable = it.hasLaunchActivity,
            isEnabled = it.isEnabled,
            installTime = it.installTime,
            updateTime = it.lastUpdateTime,
            permissions = it.requestedPermissions
        )
    }
}

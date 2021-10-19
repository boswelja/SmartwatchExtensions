package com.boswelja.smartwatchextensions.appmanager

fun AppList.mapToWatchAppDetails(sourceUid: String): List<WatchAppDetails> {
    return apps.map {
        WatchAppDetails(
            watchId = sourceUid,
            packageName = it.packageName,
            iconPath = null,
            label = it.label,
            versionName = it.version,
            versionCode = 0,
            isSystemApp = it.isSystemApp,
            isLaunchable = it.hasLaunchActivity,
            isEnabled = it.isEnabled,
            installTime = it.installTime,
            updateTime = it.lastUpdateTime,
            permissions = it.requestedPermissions
        )
    }
}

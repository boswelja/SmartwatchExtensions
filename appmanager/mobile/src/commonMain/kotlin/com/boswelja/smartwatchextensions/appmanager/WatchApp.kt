package com.boswelja.smartwatchextensions.appmanager

data class WatchAppDetails(
    val watchId: String,
    val packageName: String,
    val iconPath: String?,
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

data class WatchApp(
    val iconPath: String?,
    val label: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val isEnabled: Boolean
)

data class WatchAppVersion(
    val packageName: String,
    val updateTime: Long
)

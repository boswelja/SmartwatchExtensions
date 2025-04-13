package com.boswelja.smartwatchextensions.appmanager

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat

internal fun PackageInfo.toApp(packageManager: PackageManager): App {
    return App(
        versionName = versionName ?: "0",
        versionCode = PackageInfoCompat.getLongVersionCode(this),
        packageName = packageName,
        label = applicationInfo!!.loadLabel(packageManager).toString(),
        isSystemApp = applicationInfo!!.flags.and(
            ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        ) != 0,
        hasLaunchActivity = packageManager.getLaunchIntentForPackage(packageName) != null,
        isEnabled = applicationInfo?.enabled == true,
        installTime = firstInstallTime,
        updateTime = lastUpdateTime,
        requestedPermissions = packageManager.getLocalizedPermissions(this)
    )
}

internal fun AppList.isNotEmpty() = apps.isNotEmpty()

internal fun RemovedApps.isNotEmpty() = packages.isNotEmpty()

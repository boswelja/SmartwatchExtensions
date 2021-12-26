package com.boswelja.smartwatchextensions.appmanager

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Get all packages installed on this device, and convert them to [App] instances.
 */
fun PackageManager.getAllApps(): List<App> {
    return getInstalledPackages(PackageManager.GET_PERMISSIONS).map {
        it.toApp(this)
    }
}

/**
 * Attempts to convert system permissions strings into something meaningful to the user.
 * Fallback to the standard permission string.
 */
internal fun PackageManager.getLocalizedPermissions(
    packageInfo: PackageInfo
): List<String> {
    return packageInfo.requestedPermissions?.map { permission ->
        try {
            val permissionInfo = getPermissionInfo(permission, PackageManager.GET_META_DATA)
            permissionInfo?.loadDescription(this)?.toString() ?: permission
        } catch (_: PackageManager.NameNotFoundException) {
            permission
        }
    }?.sorted() ?: emptyList()
}

/**
 * Checks whether a package is installed.
 * @param packageName The name of the package to check.
 * @return true if the package is installed, false otherwise.
 */
internal fun PackageManager.isPackageInstalled(packageName: String): Boolean {
    return try {
        getApplicationInfo(packageName, 0)
        true
    } catch (ignored: Exception) {
        false
    }
}

/**
 * Gets a launch intent for a given package and try start a new activity for it.
 * @param packageName The name of the package to try open.
 */
fun PackageManager.launchIntent(packageName: String): Intent? {
    return getLaunchIntentForPackage(packageName)
        ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
}

/**
 * If a package is installed, shows a prompt to allow the user to uninstall it.
 * @param packageName The name of the package to try uninstall.
 */
fun PackageManager.requestUninstallIntent(packageName: String): Intent? {
    return if (isPackageInstalled(packageName)) {
        Intent().apply {
            action = Intent.ACTION_DELETE
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    } else {
        null
    }
}

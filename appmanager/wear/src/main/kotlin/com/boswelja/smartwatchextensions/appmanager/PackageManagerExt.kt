package com.boswelja.smartwatchextensions.appmanager

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.net.Uri

/**
 * Get all packages installed on this device, and convert them to [App] instances.
 */
fun PackageManager.getAllApps(): List<App> {
    return getInstalledPackages(PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())).map {
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
        getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
        true
    } catch (ignored: Exception) {
        false
    }
}

/**
 * Gets a launch intent for a given package and try start a new activity for it.
 * @param packageName The name of the package to try open.
 * @return An [Intent] that can be used to launch the given package, or null if the package isn't launchable.
 */
fun PackageManager.launchIntent(packageName: String): Intent? {
    return getLaunchIntentForPackage(packageName)
        ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
}

/**
 * If a package is installed, shows a prompt to allow the user to uninstall it.
 * @param packageName The name of the package to try uninstall.
 * @return An [Intent] that can be used to request the user uninstalls the given package, or null if
 * the package doesn't exist.
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

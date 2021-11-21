package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.pm.PackageInfoCompat

/**
 * Gets a launch intent for a given package and try start a new activity for it.
 * @param packageName The name of the package to try open.
 */
fun Context.openPackage(packageName: String) {
    packageManager.getLaunchIntentForPackage(packageName)
        ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        ?.also { startActivity(it) }
}

/**
 * If a package is installed, shows a prompt to allow the user to uninstall it.
 * @param packageName The name of the package to try uninstall.
 */
fun Context.requestUninstallPackage(packageName: String) {
    if (isPackageInstalled(packageName)) {
        Intent().apply {
            action = Intent.ACTION_DELETE
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also { startActivity(it) }
    }
}

/**
 * Get all packages installed on this device, and convert them to [App] instances.
 */
fun Context.getAllApps(): List<App> {
    return packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS).map {
        it.toApp(packageManager)
    }
}

/**
 * Attempts to convert system permissions strings into something meaningful to the user.
 * Fallback to the standard permission string.
 */
private fun PackageManager.getLocalizedPermissions(
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
private fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (ignored: Exception) {
        false
    }
}

internal fun PackageInfo.toApp(packageManager: PackageManager): App {
    return App(
        versionName = versionName,
        versionCode = PackageInfoCompat.getLongVersionCode(this),
        packageName = packageName,
        label = applicationInfo.loadLabel(packageManager).toString(),
        isSystemApp = applicationInfo.flags.and(
            ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        ) != 0,
        hasLaunchActivity = packageManager.getLaunchIntentForPackage(packageName) != null,
        isEnabled = applicationInfo.enabled,
        installTime = firstInstallTime,
        lastUpdateTime = lastUpdateTime,
        requestedPermissions = packageManager.getLocalizedPermissions(this)
    )
}

internal fun AppList.isNotEmpty() = apps.isNotEmpty()

internal fun RemovedApps.isNotEmpty() = packages.isNotEmpty()

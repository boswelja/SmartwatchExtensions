package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.AppList
import com.boswelja.smartwatchextensions.discoveryClient
import com.boswelja.smartwatchextensions.messageClient
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient

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
 * Send all apps installed to the companion phone with a given ID.
 * @param messageClient The [MessageClient] instance to use.
 */
suspend fun Context.sendAllApps(
    messageClient: MessageClient = messageClient(
        listOf(AppListSerializer)
    ),
    discoveryClient: DiscoveryClient = discoveryClient()
) {
    val pairedPhone = discoveryClient.pairedPhone()!!

    // Get all current packages
    val allApps = getAllApps()

    // Let the phone know what we're doing
    messageClient.sendMessage(
        pairedPhone,
        Message(APP_SENDING_START, null)
    )

    // Send all apps
    messageClient.sendMessage(
        pairedPhone,
        Message(
            APP_LIST,
            AppList(allApps)
        )
    )

    // Send a message notifying the phone of a successful operation
    messageClient.sendMessage(
        pairedPhone,
        Message(APP_SENDING_COMPLETE, null)
    )
}

/**
 * Get all packages installed on this device, and convert them to [App] instances.
 */
fun Context.getAllApps(): List<App> {
    return packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS).map {
        App(
            version = it.versionName,
            packageName = it.packageName,
            label = it.applicationInfo.loadLabel(packageManager).toString(),
            isSystemApp = it.applicationInfo.flags.and(
                ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
            ) != 0,
            hasLaunchActivity = packageManager.getLaunchIntentForPackage(it.packageName) != null,
            isEnabled = it.applicationInfo.enabled,
            installTime = it.firstInstallTime,
            lastUpdateTime = it.lastUpdateTime,
            requestedPermissions = packageManager.getLocalizedPermissions(it)
        )
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
            permissionInfo?.loadLabel(this)?.toString() ?: permission
        } catch (e: Exception) {
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

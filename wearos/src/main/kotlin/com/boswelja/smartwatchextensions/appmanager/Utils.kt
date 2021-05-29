package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.Messages
import com.boswelja.smartwatchextensions.common.toByteArray
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Converts a given [ByteArray] to a package name string.
 * @return THe package name, or null if data was invalid.
 */
fun ByteArray.toPackageName(): String? {
    return if (isNotEmpty()) {
        String(this, Charsets.UTF_8)
    } else {
        null
    }
}

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
 * @param phoneId The ID of the phone.
 */
suspend fun Context.sendAllApps(phoneId: String) {
    withContext(Dispatchers.IO) {
        val allPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            .map {
                App(packageManager, it)
            }

        val messageClient = Wearable.getMessageClient(this@sendAllApps)
        // Send expected app count to the phone
        val data = allPackages.count().toByteArray()
        messageClient.sendMessage(phoneId, Messages.EXPECTED_APP_COUNT, data).await()

        // Start sending apps
        allPackages.forEach { app ->
            messageClient.sendMessage(phoneId, Messages.PACKAGE_ADDED, app.toByteArray()).await()
        }
    }
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

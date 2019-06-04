/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.appmanager

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class AppPackageInfo(packageManager: PackageManager, packageInfo: PackageInfo) : Serializable {

    val versionCode: Long = PackageInfoCompat.getLongVersionCode(packageInfo)
    val versionName: String = packageInfo.versionName

    val packageName: String = packageInfo.packageName
    val packageLabel: String = getApplicationLabel(packageManager, packageInfo)

    val isSystemApp: Boolean = isSystemApp(packageInfo)
    val hasLaunchActivity: Boolean = hasLaunchActivity(packageManager)

    val installTime: Long = packageInfo.firstInstallTime
    val lastUpdateTime: Long = packageInfo.lastUpdateTime

    val requestedPermissions: Array<String>? = packageInfo.requestedPermissions

    private fun getApplicationLabel(packageManager: PackageManager, packageInfo: PackageInfo): String {
        var applicationName = packageManager.getApplicationLabel(packageInfo.applicationInfo)
        if (applicationName.isNullOrBlank()) {
            applicationName = packageName
        }
        return applicationName.toString()
    }

    private fun isSystemApp(packageInfo: PackageInfo): Boolean =
            (packageInfo.applicationInfo?.flags?.and((ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))) != 0

    private fun hasLaunchActivity(packageManager: PackageManager): Boolean =
            packageManager.getLaunchIntentForPackage(packageName) != null

    @Throws(IOException::class)
    fun toByteArray(): ByteArray {
        ByteArrayOutputStream().use {
            ObjectOutputStream(it).use { objectOutputStream -> objectOutputStream.writeObject(this) }
            return it.toByteArray()
        }
    }

    companion object {
        const val serialVersionUID: Long = 4

        @Throws(IOException::class, ClassNotFoundException::class)
        fun fromByteArray(byteArray: ByteArray): AppPackageInfo {
            ObjectInputStream(ByteArrayInputStream(byteArray)).use {
                return it.readObject() as AppPackageInfo
            }
        }
    }
}

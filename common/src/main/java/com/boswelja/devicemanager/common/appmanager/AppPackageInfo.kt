/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.devicemanager.common.SerializableBitmap
import java.io.*

data class AppPackageInfo(
    val packageIcon: SerializableBitmap?,
    val versionCode: Long,
    val versionName: String?,
    val packageName: String,
    val packageLabel: String,
    val isSystemApp: Boolean,
    val hasLaunchActivity: Boolean,
    val installTime: Long,
    val lastUpdateTime: Long,
    val requestedPermissions: Array<String>?
) : Serializable {

    constructor(packageManager: PackageManager, packageInfo: PackageInfo) : this(
        SerializableBitmap(packageManager.getApplicationIcon(packageInfo.packageName).toBitmap()),
        PackageInfoCompat.getLongVersionCode(packageInfo),
        packageInfo.versionName,
        packageInfo.packageName,
        packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
        (packageInfo.applicationInfo?.flags?.and(
            (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))) != 0,
        packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null,
        packageInfo.firstInstallTime,
        packageInfo.lastUpdateTime,
        packageInfo.requestedPermissions)

    override fun toString(): String {
        return packageLabel
    }

    override fun equals(other: Any?): Boolean {
        return if (other is AppPackageInfo) {
            packageName == other.packageName &&
                packageLabel == other.packageLabel &&
                versionCode == other.versionCode &&
                versionName == other.versionName &&
                isSystemApp == other.isSystemApp &&
                hasLaunchActivity == other.hasLaunchActivity &&
                installTime == other.installTime &&
                lastUpdateTime == other.lastUpdateTime
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = versionCode.hashCode()
        result = 31 * result + packageIcon.hashCode()
        result = 31 * result + (versionName?.hashCode() ?: 0)
        result = 31 * result + packageName.hashCode()
        result = 31 * result + packageLabel.hashCode()
        result = 31 * result + isSystemApp.hashCode()
        result = 31 * result + hasLaunchActivity.hashCode()
        result = 31 * result + installTime.hashCode()
        result = 31 * result + lastUpdateTime.hashCode()
        return result
    }

    @Throws(IOException::class)
    fun toByteArray(): ByteArray {
        ByteArrayOutputStream().use {
            ObjectOutputStream(it).use { objectOutputStream ->
                objectOutputStream.writeObject(this)
            }
            return it.toByteArray()
        }
    }

    companion object {
        const val serialVersionUID: Long = 7

        @Throws(IOException::class, ClassNotFoundException::class)
        fun fromByteArray(byteArray: ByteArray): AppPackageInfo {
            ObjectInputStream(ByteArrayInputStream(byteArray)).use {
                return it.readObject() as AppPackageInfo
            }
        }
    }
}

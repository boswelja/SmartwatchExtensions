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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class AppPackageInfo(packageManager: PackageManager, packageInfo: PackageInfo) : Serializable {

    val versionCode: Int = packageInfo.versionCode
    val versionName: String = packageInfo.versionName

    val packageName: String = packageInfo.packageName
    val label: String = getApplicationLabel(packageManager, packageInfo)

    val packageEnabled: Boolean = packageInfo.applicationInfo?.enabled == true
    val isSystemApp: Boolean = (packageInfo.applicationInfo?.flags?.and((ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))) != 0

    private fun getApplicationLabel(packageManager: PackageManager, packageInfo: PackageInfo): String {
        var applicationName = packageManager.getApplicationLabel(packageInfo.applicationInfo)
        if (applicationName.isNullOrBlank()) {
            applicationName = packageName
        }
        return applicationName.toString()
    }

    @Throws(IOException::class)
    fun toByteArray(): ByteArray {
        ByteArrayOutputStream().use {
            ObjectOutputStream(it).use { objectOutputStream -> objectOutputStream.writeObject(this) }
            return it.toByteArray()
        }
    }

    companion object {
        @Throws(IOException::class, ClassNotFoundException::class)
        fun fromByteArray(byteArray: ByteArray): AppPackageInfo {
            ObjectInputStream(ByteArrayInputStream(byteArray)).use {
                return it.readObject() as AppPackageInfo
            }
        }
    }
}

/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.appmanager

import android.content.pm.PackageManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class AppPackageInfoList(packageManager: PackageManager) : ArrayList<AppPackageInfo>(), Serializable {

    init {
        val allPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS).map {
            AppPackageInfo(packageManager, it)
        }
        addAll(filterAppsList(allPackages))
    }

    private fun filterAppsList(apps: List<AppPackageInfo>): List<AppPackageInfo> =
        apps.filter {
            (!blacklistedApps.contains(it.packageName)) &&
                ((it.isSystemApp && it.hasLaunchActivity) || (!it.isSystemApp))
        }

    @Throws(IOException::class)
    fun toByteArray(): ByteArray {
        ByteArrayOutputStream().use {
            ObjectOutputStream(it).use { objectOutputStream -> objectOutputStream.writeObject(this) }
            return it.toByteArray()
        }
    }

    companion object {
        private val blacklistedApps = arrayOf(
            "com.google.android.gms"
        )
        const val serialVersionUID: Long = 3

        fun fromByteArray(byteArray: ByteArray): AppPackageInfoList {
            ObjectInputStream(ByteArrayInputStream(byteArray)).use {
                return it.readObject() as AppPackageInfoList
            }
        }
    }
}

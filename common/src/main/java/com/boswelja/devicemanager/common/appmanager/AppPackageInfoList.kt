/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
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

class AppPackageInfoList(packageManager: PackageManager) : List<AppPackageInfo>, Serializable {

    private val appsList: List<AppPackageInfo>

    init {
        val allPackages = packageManager.getInstalledPackages(0).map {
            AppPackageInfo(packageManager, it)
        }
        appsList = filterAppsList(allPackages)
    }

    override val size: Int = appsList.size

    override fun contains(element: AppPackageInfo): Boolean = appsList.contains(element)

    override fun containsAll(elements: Collection<AppPackageInfo>): Boolean = appsList.containsAll(elements)

    override fun get(index: Int): AppPackageInfo = appsList[index]

    override fun indexOf(element: AppPackageInfo): Int = appsList.indexOf(element)

    override fun isEmpty(): Boolean = appsList.isEmpty()

    override fun iterator(): Iterator<AppPackageInfo> = appsList.iterator()

    override fun lastIndexOf(element: AppPackageInfo): Int = appsList.lastIndexOf(element)

    override fun listIterator(): ListIterator<AppPackageInfo> = appsList.listIterator()

    override fun listIterator(index: Int): ListIterator<AppPackageInfo> = appsList.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<AppPackageInfo> = appsList.subList(fromIndex, toIndex)

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
        const val serialVersionUID: Long = 2

        fun fromByteArray(byteArray: ByteArray): AppPackageInfoList {
            ObjectInputStream(ByteArrayInputStream(byteArray)).use {
                return it.readObject() as AppPackageInfoList
            }
        }
    }
}

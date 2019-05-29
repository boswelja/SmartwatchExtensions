package com.boswelja.devicemanager.common.appmanager

import android.content.pm.ApplicationInfo
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
        appsList = packageManager.getInstalledPackages(0).filter { packageInfo ->
            (packageInfo.applicationInfo?.flags?.and((ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))) == 0
        }.map { AppPackageInfo(packageManager, it) }
    }

    override val size: Int
        get() = appsList.size

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

    @Throws(IOException::class)
    fun toByteArray(): ByteArray {
        ByteArrayOutputStream().use {
            ObjectOutputStream(it).use { objectOutputStream -> objectOutputStream.writeObject(this) }
            return it.toByteArray()
        }
    }

    companion object {
        const val serialVersionUID: Long = 4638100436427186094L

        fun fromByteArray(byteArray: ByteArray): AppPackageInfoList {
            ObjectInputStream(ByteArrayInputStream(byteArray)).use {
                return it.readObject() as AppPackageInfoList
            }
        }
    }
}
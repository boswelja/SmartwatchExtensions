package com.boswelja.devicemanager.common.appmanager

import android.content.pm.PackageInfo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class AppPackageInfo(packageInfo: PackageInfo) : Serializable {

    val versionCode: Int = packageInfo.versionCode
    val versionName: String = packageInfo.versionName

    val packageName: String = packageInfo.packageName

    val packageEnabled: Boolean = packageInfo.applicationInfo?.enabled == true

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
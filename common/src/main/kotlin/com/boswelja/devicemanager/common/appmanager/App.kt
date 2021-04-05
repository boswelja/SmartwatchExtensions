package com.boswelja.devicemanager.common.appmanager

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.devicemanager.common.SerializableBitmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * A data class to store information we want to use from [PackageInfo] in a serializable format.
 * @param icon A [SerializableBitmap] representing the package icon, or null if no icon is found.
 * @param version The version of the package. Will default to [PackageInfo.versionName], and fall
 * back to [PackageInfoCompat.getLongVersionCode] if we can't get the version name.
 * @param packageName The [PackageInfo.packageName] of the package.
 * @param label The user-facing name for the package. See [PackageManager.getApplicationLabel].
 * @param isSystemApp A boolean to determine whether the package is a system app.
 * @param hasLaunchActivity A boolean to determine whether the package is launchable.
 * @param installTime The time in milliseconds this package was first installed.
 * @param lastUpdateTime The time in milliseconds this package was last updated.
 * @param requestedPermissions An [Array] of [android.Manifest.permission]s this package requests.
 */
data class App(
    val icon: SerializableBitmap?,
    val version: String,
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val hasLaunchActivity: Boolean,
    val installTime: Long,
    val lastUpdateTime: Long,
    val requestedPermissions: Array<String>
) : Serializable {

    constructor(packageManager: PackageManager, packageInfo: PackageInfo) : this(
        SerializableBitmap(packageManager.getApplicationIcon(packageInfo.packageName).toBitmap()),
        getVersion(packageInfo),
        packageInfo.packageName,
        packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
        isSystemApp(packageInfo.applicationInfo),
        packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null,
        packageInfo.firstInstallTime,
        packageInfo.lastUpdateTime,
        getLocalizedPermissions(packageManager, packageInfo)
    )

    override fun toString(): String {
        return label
    }

    override fun equals(other: Any?): Boolean {
        return if (other is App) {
            packageName == other.packageName &&
                label == other.label &&
                version == other.version &&
                isSystemApp == other.isSystemApp &&
                hasLaunchActivity == other.hasLaunchActivity &&
                installTime == other.installTime &&
                lastUpdateTime == other.lastUpdateTime
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + label.hashCode()
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
        const val serialVersionUID: Long = 8

        @Throws(IOException::class, ClassNotFoundException::class)
        fun fromByteArray(byteArray: ByteArray): App {
            ObjectInputStream(ByteArrayInputStream(byteArray)).use {
                return it.readObject() as App
            }
        }

        /**
         * Attempts to convert system permissions strings into something meaningful to the user.
         * Fallback is to just use the system strings.
         */
        private fun getLocalizedPermissions(
            packageManager: PackageManager,
            packageInfo: PackageInfo
        ): Array<String> {
            val processedPermissions = ArrayList<String>()
            packageInfo.requestedPermissions?.forEach { permission ->
                val localizedPermission = try {
                    val permissionInfo =
                        packageManager.getPermissionInfo(
                            permission, PackageManager.GET_META_DATA
                        )
                    permissionInfo?.loadLabel(packageManager)?.toString() ?: permission
                } catch (e: Exception) {
                    permission
                }
                processedPermissions.add(localizedPermission)
            }
            processedPermissions.sort()
            return processedPermissions.toTypedArray()
        }

        private fun isSystemApp(applicationInfo: ApplicationInfo): Boolean {
            return applicationInfo.flags.and(
                ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
            ) != 0
        }

        private fun getVersion(packageInfo: PackageInfo): String {
            return packageInfo.versionName
                ?: PackageInfoCompat.getLongVersionCode(packageInfo).toString()
        }
    }
}

package com.boswelja.devicemanager.phonelocking

import android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService

object Utils {

    /**
     * Checks whether Device Administrator is enabled for this package.
     * @return true if this package is a Device Administrator, false otherwise.
     */
    fun Context.isDeviceAdminEnabled(): Boolean =
        getSystemService<DevicePolicyManager>()
            ?.isAdminActive(ComponentName(this, DeviceAdminChangeReceiver::class.java)) ?: false

    /**
     * Checks whether this package has an accessibility service enabled.
     * @return true if accessibility service is enabled, false otherwise.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun Context.isAccessibilityServiceEnabled(): Boolean {
        return getSystemService<AccessibilityManager>()
            ?.getEnabledAccessibilityServiceList(FEEDBACK_GENERIC)
            ?.any { it.resolveInfo.serviceInfo.packageName == packageName } ?: false
    }
}

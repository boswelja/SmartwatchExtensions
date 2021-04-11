package com.boswelja.devicemanager.phonelocking

import android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService

object Utils {

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

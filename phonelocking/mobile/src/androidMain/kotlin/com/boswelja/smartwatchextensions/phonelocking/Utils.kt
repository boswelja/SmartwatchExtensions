package com.boswelja.smartwatchextensions.phonelocking

import android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
import android.content.Context
import android.view.accessibility.AccessibilityManager

/**
 * Checks whether this package has an accessibility service enabled.
 * @return true if accessibility service is enabled, false otherwise.
 */
fun Context.isAccessibilityServiceEnabled(): Boolean {
    return getSystemService(AccessibilityManager::class.java)
        .getEnabledAccessibilityServiceList(FEEDBACK_GENERIC)
        .any { it.resolveInfo.serviceInfo.packageName == packageName }
}

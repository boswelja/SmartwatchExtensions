package com.boswelja.smartwatchextensions.analytics

import android.util.Log

/**
 * An [Analytics] implementation that logs to LogCat.
 */
class LoggingAnalytics : Analytics {

    private val tag = "LoggingAnalytics"

    private var enabled: Boolean = false

    override fun setAnalyticsEnabled(isEnabled: Boolean) {
        enabled = isEnabled
        Log.i(tag, "setAnalyticsEnabled($isEnabled)")
    }

    override fun logExtensionSettingChanged(key: String, value: Any) {
        if (enabled) Log.i(tag, "logExtensionSettingChanged($key, $value)")
    }

    override fun logAppSettingChanged(key: String, value: Any) {
        if (enabled) Log.i(tag, "logAppSettingChanged($key, $value)")
    }

    override fun logWatchRegistered() {
        if (enabled) Log.i(tag, "logWatchRegistered()")
    }

    override fun logWatchRemoved() {
        if (enabled) Log.i(tag, "logWatchRemoved()")
    }

    override fun logWatchRenamed() {
        if (enabled) Log.i(tag, "logWatchRenamed()")
    }

    override fun resetAnalytics() {
        Log.i(tag, "resetAnalytics()")
    }
}

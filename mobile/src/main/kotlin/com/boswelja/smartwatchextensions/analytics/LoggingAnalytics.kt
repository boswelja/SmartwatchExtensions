package com.boswelja.smartwatchextensions.analytics

import timber.log.Timber

class LoggingAnalytics : Analytics {

    private var enabled: Boolean = false

    override fun setAnalyticsEnabled(isEnabled: Boolean) {
        enabled = isEnabled
        Timber.i("setAnalyticsEnabled(%s)", isEnabled)
    }

    override fun logExtensionSettingChanged(key: String, value: Any) {
        if (enabled) Timber.i("logExtensionSettingChanged(%s, %s)", key, value)
    }

    override fun logAppSettingChanged(key: String, value: Any) {
        if (enabled) Timber.i("logAppSettingChanged(%s, %s)", key, value)
    }

    override fun logWatchRegistered() {
        if (enabled) Timber.i("logWatchRegistered()")
    }

    override fun logWatchRemoved() {
        if (enabled) Timber.i("logWatchRemoved()")
    }

    override fun logWatchRenamed() {
        if (enabled) Timber.i("logWatchRenamed()")
    }

    override fun resetAnalytics() {
        Timber.i("resetAnalytics()")
    }
}

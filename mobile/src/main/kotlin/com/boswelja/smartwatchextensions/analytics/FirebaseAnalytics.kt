package com.boswelja.smartwatchextensions.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Wrapper for [FirebaseAnalytics] to simplify logging analytics events.
 */
class FirebaseAnalytics : Analytics {

    private val analytics = Firebase.analytics

    override fun setAnalyticsEnabled(isEnabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(isEnabled)
    }

    override fun logExtensionSettingChanged(key: String, value: Any) {
        analytics.logEvent(EVENT_EXTENSION_SETTING_CHANGED) {
            param(FirebaseAnalytics.Param.SOURCE, key)
        }
    }

    override fun logAppSettingChanged(key: String, value: Any) {
        analytics.logEvent(EVENT_APP_SETTING_CHANGED) {
            param(FirebaseAnalytics.Param.SOURCE, key)
        }
    }

    override fun logWatchRegistered() {
        analytics.logEvent(EVENT_WATCH_REGISTERED, null)
    }

    override fun logWatchRemoved() {
        analytics.logEvent(EVENT_WATCH_REMOVED, null)
    }

    override fun logWatchRenamed() {
        analytics.logEvent(EVENT_WATCH_RENAMED, null)
    }

    override fun resetAnalytics() {
        analytics.resetAnalyticsData()
    }

    companion object {
        internal const val EVENT_EXTENSION_SETTING_CHANGED = "extension_setting_changed"
        internal const val EVENT_APP_SETTING_CHANGED = "app_setting_changed"
        internal const val EVENT_WATCH_REGISTERED = "watch_registered"
        internal const val EVENT_WATCH_REMOVED = "watch_registered"
        internal const val EVENT_WATCH_RENAMED = "watch_renamed"
    }
}

package com.boswelja.devicemanager.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Wrapper for [FirebaseAnalytics] to simplify logging analytics events.
 */
class Analytics constructor(private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics) {

    /**
     * Set whether analytics collection is enabled.
     */
    fun setAnalyticsEnabled(isEnabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(isEnabled)
    }

    /**
     * Log an extension setting change.
     * @param key The key for the preference that was changed.
     * @param value The value of the changed preference.
     */
    fun logExtensionSettingChanged(key: String, value: Any) {
        firebaseAnalytics.logEvent(EVENT_EXTENSION_SETTING_CHANGED) {
            param(FirebaseAnalytics.Param.ITEM_ID, key)
            param(FirebaseAnalytics.Param.VALUE, value.toString())
        }
    }

    /**
     * Log an app setting change.
     * @param key The key for the preference that was changed.
     */
    fun logAppSettingChanged(key: String) {
        firebaseAnalytics.logEvent(EVENT_APP_SETTING_CHANGED) {
            param(FirebaseAnalytics.Param.ITEM_ID, key)
        }
    }

    /**
     * Log a new watch being registered.
     */
    fun logWatchRegistered() {
        firebaseAnalytics.logEvent(EVENT_WATCH_REGISTERED, null)
    }

    /**
     * Log an existing watch being removed.
     */
    fun logWatchRemoved() {
        firebaseAnalytics.logEvent(EVENT_WATCH_REMOVED, null)
    }

    /**
     * Log a watch being renamed.
     */
    fun logWatchRenamed() {
        firebaseAnalytics.logEvent(EVENT_WATCH_RENAMED, null)
    }

    /**
     * Log an action being performed in StorageManager.
     */
    fun logStorageManagerAction(action: String) {
        firebaseAnalytics.logEvent(EVENT_STORAGE_MANAGER) {
            param(FirebaseAnalytics.Param.METHOD, action)
        }
    }

    /**
     * Resets all analytics data. See [FirebaseAnalytics.resetAnalyticsData].
     */
    fun resetAnalytics() {
        firebaseAnalytics.resetAnalyticsData()
    }

    companion object {
        const val ANALYTICS_ENABLED_KEY = "send_analytics"

        internal const val EVENT_EXTENSION_SETTING_CHANGED = "extension_setting_changed"
        internal const val EVENT_APP_SETTING_CHANGED = "app_setting_changed"
        internal const val EVENT_WATCH_REGISTERED = "watch_registered"
        internal const val EVENT_WATCH_REMOVED = "watch_registered"
        internal const val EVENT_WATCH_RENAMED = "watch_renamed"
        internal const val EVENT_STORAGE_MANAGER = "storage_manager"
    }
}

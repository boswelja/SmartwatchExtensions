package com.boswelja.devicemanager.analytics

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

class Analytics(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    fun logExtensionSettingChanged(key: String, value: String) {
        if (sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, false)) {
            firebaseAnalytics.logEvent(EVENT_EXTENSION_SETTING_CHANGED) {
                param(FirebaseAnalytics.Param.ITEM_ID, key)
                param(FirebaseAnalytics.Param.VALUE, value)
            }
        }
    }

    fun logAppSettingChanged(key: String, value: String) {
        if (sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, false)) {
            firebaseAnalytics.logEvent(EVENT_APP_SETTING_CHANGED) {
                param(FirebaseAnalytics.Param.ITEM_ID, key)
                param(FirebaseAnalytics.Param.VALUE, value)
            }
        }
    }

    fun logWatchRegistered() {
        if (sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, false)) {
            firebaseAnalytics.logEvent(EVENT_WATCH_REGISTERED, null)
        }
    }

    fun logWatchRemoved() {
        if (sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, false)) {
            firebaseAnalytics.logEvent(EVENT_WATCH_REMOVED, null)
        }
    }

    fun logStorageManagerAction(action: String) {
        if (sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, false)) {
            firebaseAnalytics.logEvent(EVENT_STORAGE_MANAGER) {
                param(FirebaseAnalytics.Param.METHOD, action)
            }
        }
    }

    companion object {
        const val ANALYTICS_ENABLED_KEY = "send_analytics"

        private const val EVENT_EXTENSION_SETTING_CHANGED = "extension_setting_changed"
        private const val EVENT_APP_SETTING_CHANGED = "app_setting_changed"
        private const val EVENT_WATCH_REGISTERED = "watch_registered"
        private const val EVENT_WATCH_REMOVED = "watch_registered"
        private const val EVENT_STORAGE_MANAGER = "storage_manager"
    }
}

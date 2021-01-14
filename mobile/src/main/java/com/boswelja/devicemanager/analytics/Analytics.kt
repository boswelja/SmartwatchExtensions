package com.boswelja.devicemanager.analytics

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

class Analytics {

    private val sharedPreferences: SharedPreferences
    private val firebaseAnalytics: FirebaseAnalytics

    internal constructor(
        firebaseAnalytics: FirebaseAnalytics,
        sharedPreferences: SharedPreferences
    ) {
        this.sharedPreferences = sharedPreferences
        this.firebaseAnalytics = firebaseAnalytics
    }

    constructor(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun logSettingChanged(key: String, value: Any) {
        if (sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, false)) {
            if (key in SyncPreferences.ALL_PREFS) {
                logExtensionSettingChanged(key, value)
            } else {
                logAppSettingChanged(key, value)
            }
        }
    }

    fun logExtensionSettingChanged(key: String, value: Any) {
        if (sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, false)) {
            firebaseAnalytics.logEvent(EVENT_EXTENSION_SETTING_CHANGED) {
                param(FirebaseAnalytics.Param.ITEM_ID, key)
                param(FirebaseAnalytics.Param.VALUE, value.toString())
            }
        }
    }

    fun logAppSettingChanged(key: String, value: Any) {
        if (sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, false)) {
            firebaseAnalytics.logEvent(EVENT_APP_SETTING_CHANGED) {
                param(FirebaseAnalytics.Param.ITEM_ID, key)
                param(FirebaseAnalytics.Param.VALUE, value.toString())
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

        internal const val EVENT_EXTENSION_SETTING_CHANGED = "extension_setting_changed"
        internal const val EVENT_APP_SETTING_CHANGED = "app_setting_changed"
        internal const val EVENT_WATCH_REGISTERED = "watch_registered"
        internal const val EVENT_WATCH_REMOVED = "watch_registered"
        internal const val EVENT_STORAGE_MANAGER = "storage_manager"
    }
}

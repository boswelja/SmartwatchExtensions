package com.boswelja.smartwatchextensions.analytics

import com.boswelja.smartwatchextensions.BuildConfig

/**
 * Get an instance of [Analytics] to use.
 */
fun getAnalytics(): Analytics {
    return if (BuildConfig.DEBUG) {
        LoggingAnalytics()
    } else {
        FirebaseAnalytics()
    }
}

/**
 * A generic interface for analytic collection
 */
interface Analytics {

    /**
     * Set whether analytics collection is enabled.
     */
    fun setAnalyticsEnabled(isEnabled: Boolean)

    /**
     * Log an extension setting change.
     * @param key The key for the preference that was changed.
     * @param value The value of the changed setting.
     */
    fun logExtensionSettingChanged(key: String, value: Any)

    /**
     * Log an app setting change.
     * @param key The key for the preference that was changed.
     * @param value The value of the changed setting.
     */
    fun logAppSettingChanged(key: String, value: Any)

    /**
     * Log a new watch being registered.
     */
    fun logWatchRegistered()

    /**
     * Log an existing watch being removed.
     */
    fun logWatchRemoved()

    /**
     * Log a watch being renamed.
     */
    fun logWatchRenamed()

    /**
     * Resets all analytics data.
     */
    fun resetAnalytics()
}

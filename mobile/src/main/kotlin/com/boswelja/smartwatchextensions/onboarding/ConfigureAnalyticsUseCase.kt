package com.boswelja.smartwatchextensions.onboarding

import androidx.datastore.core.DataStore
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.settings.Settings

/**
 * A use case to aid configuring analytics.
 */
class ConfigureAnalyticsUseCase(
    private val analytics: Analytics,
    private val appSettingsStore: DataStore<Settings>
) {

    /**
     * Set whether analytics are enabled.
     * @param enabled true to enable analytics, false otherwise.
     */
    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        analytics.setAnalyticsEnabled(enabled)
        appSettingsStore.updateData { it.copy(analyticsEnabled = enabled) }
    }
}

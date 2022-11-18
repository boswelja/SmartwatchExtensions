package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SetWatchLowNotificationEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val selectedWatchManager: SelectedWatchManager
) {
    suspend operator fun invoke(
        watchId: String,
        watchLowNotificationEnabled: Boolean
    ): Boolean {
        // Set the new value
        settingsRepository.putBoolean(watchId, BATTERY_WATCH_LOW_NOTI_KEY, watchLowNotificationEnabled)
        return true
    }

    suspend operator fun invoke(watchLowNotificationEnabled: Boolean): Boolean {
        val watchId = selectedWatchManager.selectedWatch
            .map { it?.uid }
            .first()
        if (watchId.isNullOrBlank()) return false
        return invoke(watchId, watchLowNotificationEnabled)
    }
}

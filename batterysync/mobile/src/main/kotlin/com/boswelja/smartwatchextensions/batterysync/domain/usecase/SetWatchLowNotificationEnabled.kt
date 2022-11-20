package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.first

class SetWatchLowNotificationEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val selectedWatchController: SelectedWatchController
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
        val watchId = selectedWatchController.selectedWatch.first()
        if (watchId.isNullOrBlank()) return false
        return invoke(watchId, watchLowNotificationEnabled)
    }
}

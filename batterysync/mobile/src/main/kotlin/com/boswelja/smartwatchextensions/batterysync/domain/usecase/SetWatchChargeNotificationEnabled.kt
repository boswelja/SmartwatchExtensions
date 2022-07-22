package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SetWatchChargeNotificationEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val selectedWatchManager: SelectedWatchManager
) {
    suspend operator fun invoke(
        watchId: String,
        watchChargeNotificationEnabled: Boolean
    ): Boolean {
        // Set the new value
        settingsRepository.putBoolean(watchId, BATTERY_WATCH_CHARGE_NOTI_KEY, watchChargeNotificationEnabled)
        return true
    }

    suspend operator fun invoke(watchChargeNotificationEnabled: Boolean): Boolean {
        val watchId = selectedWatchManager.selectedWatch
            .map { it?.uid }
            .first()
        if (watchId.isNullOrBlank()) return false
        return invoke(watchId, watchChargeNotificationEnabled)
    }
}

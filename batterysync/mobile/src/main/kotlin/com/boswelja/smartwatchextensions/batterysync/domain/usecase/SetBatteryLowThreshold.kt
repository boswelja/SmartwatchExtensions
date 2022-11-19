package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.settings.IntSetting
import com.boswelja.smartwatchextensions.core.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateIntSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SetBatteryLowThreshold(
    private val settingsRepository: WatchSettingsRepository,
    private val messageClient: MessageClient,
    private val selectedWatchController: SelectedWatchController
) {
    suspend operator fun invoke(watchId: String, batteryLowThreshold: Int): Boolean {
        // Update the watches local value
        val updateSent = messageClient.sendMessage(
            watchId,
            Message(
                UpdateIntSetting,
                IntSettingSerializer.serialize(IntSetting(BATTERY_LOW_THRESHOLD_KEY, batteryLowThreshold))
            )
        )
        if (!updateSent) return false

        // Set the new value
        settingsRepository.putInt(watchId, BATTERY_LOW_THRESHOLD_KEY, batteryLowThreshold)
        return true
    }

    suspend operator fun invoke(batteryLowThreshold: Int): Boolean {
        val watchId = selectedWatchController.selectedWatch
            .map { it?.uid }
            .first()
        if (watchId.isNullOrBlank()) return false
        return invoke(watchId, batteryLowThreshold)
    }
}

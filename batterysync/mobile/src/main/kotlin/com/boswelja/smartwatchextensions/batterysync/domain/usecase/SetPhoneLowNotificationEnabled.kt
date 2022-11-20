package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateIntSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import kotlinx.coroutines.flow.first

class SetPhoneLowNotificationEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val messageClient: MessageClient,
    private val selectedWatchController: SelectedWatchController
) {
    suspend operator fun invoke(
        watchId: String,
        phoneLowNotificationEnabled: Boolean
    ): Boolean {
        // Update the watches local value
        val updateSent = messageClient.sendMessage(
            watchId,
            Message(
                UpdateIntSetting,
                BoolSettingSerializer.serialize(
                    BoolSetting(BATTERY_PHONE_LOW_NOTI_KEY, phoneLowNotificationEnabled)
                )
            )
        )
        if (!updateSent) return false

        // Set the new value
        settingsRepository.putBoolean(watchId, BATTERY_PHONE_LOW_NOTI_KEY, phoneLowNotificationEnabled)
        return true
    }

    suspend operator fun invoke(phoneLowNotificationEnabled: Boolean): Boolean {
        val watchId = selectedWatchController.selectedWatch.first()
        if (watchId.isNullOrBlank()) return false
        return invoke(watchId, phoneLowNotificationEnabled)
    }
}

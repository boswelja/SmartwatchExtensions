package com.boswelja.smartwatchextensions.phonelocking.domain.usecase

import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingSettingKeys.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import kotlinx.coroutines.flow.first

class SetPhoneLockingEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val selectedWatchManager: SelectedWatchManager,
    private val messageClient: MessageClient
) {
    suspend operator fun invoke(watchId: String, phoneLockingEnabled: Boolean): Boolean {
        val notifyDeviceSuccess = messageClient.sendMessage(
            watchId,
            Message(
                UpdateBoolSetting,
                BoolSettingSerializer.serialize(BoolSetting(PHONE_LOCKING_ENABLED_KEY, phoneLockingEnabled))
            )
        )

        if (!notifyDeviceSuccess) return false

        settingsRepository.putBoolean(watchId, PHONE_LOCKING_ENABLED_KEY, phoneLockingEnabled)

        return true
    }

    suspend operator fun invoke(phoneLockingEnabled: Boolean): Boolean {
        val selectedWatch = selectedWatchManager.selectedWatch.first() ?: return false
        return invoke(selectedWatch.uid, phoneLockingEnabled)
    }
}

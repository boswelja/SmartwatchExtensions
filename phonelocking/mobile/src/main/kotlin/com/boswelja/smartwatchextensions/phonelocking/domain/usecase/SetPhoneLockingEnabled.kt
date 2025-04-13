package com.boswelja.smartwatchextensions.phonelocking.domain.usecase

import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingSettingKeys.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.wearable.ext.sendMessage
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.flow.first

class SetPhoneLockingEnabled(
    private val settingsRepository: WatchSettingsRepository,
    private val selectedWatchController: SelectedWatchController,
    private val messageClient: MessageClient
) {
    suspend operator fun invoke(watchId: String, phoneLockingEnabled: Boolean): Boolean {
        val notifyDeviceSuccess = messageClient.sendMessage(
            targetId = watchId,
            path = UpdateBoolSetting,
            data = BoolSettingSerializer.serialize(BoolSetting(PHONE_LOCKING_ENABLED_KEY, phoneLockingEnabled))
        )

        if (!notifyDeviceSuccess) return false

        settingsRepository.putBoolean(watchId, PHONE_LOCKING_ENABLED_KEY, phoneLockingEnabled)

        return true
    }

    suspend operator fun invoke(phoneLockingEnabled: Boolean): Boolean {
        val selectedWatch = selectedWatchController.selectedWatch.first() ?: return false
        return invoke(selectedWatch.uid, phoneLockingEnabled)
    }
}

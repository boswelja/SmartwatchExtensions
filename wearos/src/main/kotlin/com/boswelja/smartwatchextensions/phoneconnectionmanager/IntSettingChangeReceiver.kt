package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatterySyncStateRepository
import com.boswelja.smartwatchextensions.settings.IntSetting
import com.boswelja.smartwatchextensions.settings.IntSettingKeys
import com.boswelja.smartwatchextensions.settings.IntSettingSerializer
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [IntSetting].
 */
class IntSettingChangeReceiver : MessageReceiver<IntSetting>(IntSettingSerializer), KoinComponent {

    private val batterySyncStateRepository: BatterySyncStateRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<IntSetting>) {
        handleIntPreferenceChange(message.data.key, message.data.value)
    }

    private suspend fun handleIntPreferenceChange(key: String, value: Int) {
        when (key) {
            IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY ->
                batterySyncStateRepository.updateBatterySyncState {
                    it.copy(phoneChargeThreshold = value)
                }
            IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY ->
                batterySyncStateRepository.updateBatterySyncState {
                    it.copy(phoneLowThreshold = value)
                }
        }
    }
}

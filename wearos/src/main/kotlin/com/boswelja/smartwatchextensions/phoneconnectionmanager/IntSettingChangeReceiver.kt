package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import com.boswelja.smartwatchextensions.core.settings.IntSetting
import com.boswelja.smartwatchextensions.core.settings.IntSettingSerializer
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [IntSetting].
 */
class IntSettingChangeReceiver : MessageReceiver<IntSetting>(IntSettingSerializer), KoinComponent {

    private val batterySyncConfigRepository: BatterySyncConfigRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<IntSetting>) {
        handleIntPreferenceChange(message.data.key, message.data.value)
    }

    private suspend fun handleIntPreferenceChange(key: String, value: Int) {
        when (key) {
            BATTERY_CHARGE_THRESHOLD_KEY ->
                batterySyncConfigRepository.updateBatterySyncState {
                    it.copy(phoneChargeThreshold = value)
                }
            BATTERY_LOW_THRESHOLD_KEY ->
                batterySyncConfigRepository.updateBatterySyncState {
                    it.copy(phoneLowThreshold = value)
                }
        }
    }
}

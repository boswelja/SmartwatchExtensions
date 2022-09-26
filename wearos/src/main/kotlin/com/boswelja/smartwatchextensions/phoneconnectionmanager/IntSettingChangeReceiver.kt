package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import com.boswelja.smartwatchextensions.core.settings.IntSetting
import com.boswelja.smartwatchextensions.core.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateIntSetting
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [IntSetting].
 */
class IntSettingChangeReceiver : MessageReceiver(), KoinComponent {

    private val batterySyncConfigRepository: BatterySyncConfigRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == UpdateIntSetting) {
            val pref = message.data?.let { IntSettingSerializer.deserialize(it) } ?: return
            handleIntPreferenceChange(pref.key, pref.value)
        }
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

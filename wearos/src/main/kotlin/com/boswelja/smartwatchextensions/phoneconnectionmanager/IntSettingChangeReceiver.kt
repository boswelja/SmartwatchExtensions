package com.boswelja.smartwatchextensions.phoneconnectionmanager

import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import com.boswelja.smartwatchextensions.core.settings.IntSetting
import com.boswelja.smartwatchextensions.core.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateIntSetting
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [WearableListenerService] for receiving [IntSetting].
 */
class IntSettingChangeReceiver : WearableListenerService(), KoinComponent {

    private val batterySyncConfigRepository: BatterySyncConfigRepository by inject()

    override fun onMessageReceived(message: MessageEvent) {
        if (message.path == UpdateIntSetting) {
            val pref = IntSettingSerializer.deserialize(message.data)
            runBlocking { handleIntPreferenceChange(pref.key, pref.value) }
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

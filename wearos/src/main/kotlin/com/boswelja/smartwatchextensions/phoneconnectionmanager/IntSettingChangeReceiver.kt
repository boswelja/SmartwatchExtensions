package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.settings.IntSetting
import com.boswelja.smartwatchextensions.settings.IntSettingKeys
import com.boswelja.smartwatchextensions.settings.IntSettingSerializer
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver

/**
 * A [MessageReceiver] for receiving [IntSetting].
 */
class IntSettingChangeReceiver : MessageReceiver<IntSetting>(IntSettingSerializer) {

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<IntSetting>) {
        handleIntPreferenceChange(context, message.data.key, message.data.value)
    }

    private suspend fun handleIntPreferenceChange(context: Context, key: String, value: Int) {
        context.extensionSettingsStore.updateData {
            var batteryLowThreshold = it.batteryLowThreshold
            var batteryChargeThreshold = it.batteryChargeThreshold
            when (key) {
                IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY ->
                    batteryChargeThreshold = value
                IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY ->
                    batteryLowThreshold = value
            }
            it.copy(
                batteryLowThreshold = batteryLowThreshold,
                batteryChargeThreshold = batteryChargeThreshold
            )
        }
    }
}

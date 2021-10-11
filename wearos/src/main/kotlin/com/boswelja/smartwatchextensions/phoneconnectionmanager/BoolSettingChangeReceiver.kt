package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.boswelja.smartwatchextensions.batterysync.PhoneBatteryComplicationProvider
import com.boswelja.smartwatchextensions.dndsync.LocalDnDAndTheaterCollectorService
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.settings.BoolSetting
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys
import com.boswelja.smartwatchextensions.settings.BoolSettingSerializer
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

class BoolSettingChangeReceiver : MessageReceiver<BoolSetting>(BoolSettingSerializer) {

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BoolSetting>
    ) {
        handleBoolPreferenceChange(context, message.data.key, message.data.value)
    }

    private suspend fun handleBoolPreferenceChange(
        context: Context,
        key: String,
        value: Boolean
    ) {
        context.extensionSettingsStore.updateData {
            var batterySyncEnabled = it.batterySyncEnabled
            var phoneLockingEnabled = it.phoneLockingEnabled
            var phoneChargeNotiEnabled = it.phoneChargeNotiEnabled
            var phoneLowNotiEnabled = it.phoneLowNotiEnabled
            var dndSyncToPhone = it.dndSyncToPhone
            var dndSyncWithTheater = it.dndSyncWithTheater
            var phoneSeparationNotis = it.phoneSeparationNotis

            when (key) {
                BoolSettingKeys.PHONE_LOCKING_ENABLED_KEY ->
                    phoneLockingEnabled = value
                BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY -> {
                    batterySyncEnabled = value
                    PhoneBatteryComplicationProvider.updateAll(context)
                }
                BoolSettingKeys.BATTERY_PHONE_CHARGE_NOTI_KEY ->
                    phoneChargeNotiEnabled = value
                BoolSettingKeys.BATTERY_PHONE_LOW_NOTI_KEY ->
                    phoneLowNotiEnabled = value
                BoolSettingKeys.DND_SYNC_TO_PHONE_KEY -> {
                    dndSyncToPhone = value
                    if (dndSyncToPhone) {
                        startDnDListenerService(context)
                    }
                }
                BoolSettingKeys.DND_SYNC_WITH_THEATER_KEY -> {
                    dndSyncWithTheater = value
                    if (dndSyncWithTheater) {
                        startDnDListenerService(context)
                    }
                }
                BoolSettingKeys.PHONE_SEPARATION_NOTI_KEY -> {
                    phoneSeparationNotis = value
                    if (phoneSeparationNotis) {
                        SeparationObserverService.start(context)
                    }
                }
            }

            it.copy(
                phoneLockingEnabled = phoneLockingEnabled,
                batterySyncEnabled = batterySyncEnabled,
                phoneChargeNotiEnabled = phoneChargeNotiEnabled,
                phoneLowNotiEnabled = phoneLowNotiEnabled,
                dndSyncToPhone = dndSyncToPhone,
                dndSyncWithTheater = dndSyncWithTheater,
                phoneSeparationNotis = phoneSeparationNotis
            )
        }
    }

    private fun startDnDListenerService(context: Context) {
        Intent(context, LocalDnDAndTheaterCollectorService::class.java).also {
            ContextCompat.startForegroundService(context, it)
        }
    }
}

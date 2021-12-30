package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.boswelja.smartwatchextensions.batterysync.BatterySyncStateRepository
import com.boswelja.smartwatchextensions.batterysync.PhoneBatteryComplicationProvider
import com.boswelja.smartwatchextensions.dndsync.LocalDnDAndTheaterCollectorService
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.settings.BoolSetting
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys
import com.boswelja.smartwatchextensions.settings.BoolSettingSerializer
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [BoolSetting].
 */
class BoolSettingChangeReceiver : MessageReceiver<BoolSetting>(BoolSettingSerializer), KoinComponent {

    private val batterySyncStateRepository: BatterySyncStateRepository by inject()

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
            var phoneLockingEnabled = it.phoneLockingEnabled
            var dndSyncToPhone = it.dndSyncToPhone
            var dndSyncWithTheater = it.dndSyncWithTheater
            var phoneSeparationNotis = it.phoneSeparationNotis

            when (key) {
                BoolSettingKeys.PHONE_LOCKING_ENABLED_KEY ->
                    phoneLockingEnabled = value
                BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY -> {
                    batterySyncStateRepository.updateBatterySyncState {
                        it.copy(batterySyncEnabled = value)
                    }
                    PhoneBatteryComplicationProvider.updateAll(context)
                }
                BoolSettingKeys.BATTERY_PHONE_CHARGE_NOTI_KEY ->
                    batterySyncStateRepository.updateBatterySyncState {
                        it.copy(phoneChargeNotificationEnabled = value)
                    }
                BoolSettingKeys.BATTERY_PHONE_LOW_NOTI_KEY ->
                    batterySyncStateRepository.updateBatterySyncState {
                        it.copy(phoneLowNotificationEnabled = value)
                    }
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

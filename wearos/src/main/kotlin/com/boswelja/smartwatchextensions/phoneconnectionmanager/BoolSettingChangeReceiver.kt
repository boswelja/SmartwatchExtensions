package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncStateRepository
import com.boswelja.smartwatchextensions.batterysync.PhoneBatteryComplicationProvider
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.dndsync.DnDSyncStateRepository
import com.boswelja.smartwatchextensions.dndsync.LocalDnDAndTheaterCollectorService
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingSettingKeys.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingStateRepository
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.proximity.common.ProximitySettingKeys.PHONE_SEPARATION_NOTI_KEY
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [BoolSetting].
 */
class BoolSettingChangeReceiver : MessageReceiver<BoolSetting>(BoolSettingSerializer), KoinComponent {

    private val batterySyncStateRepository: BatterySyncStateRepository by inject()
    private val dndSyncStateRepository: DnDSyncStateRepository by inject()
    private val phoneLockingStateRepository: PhoneLockingStateRepository by inject()

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
        when (key) {
            PHONE_LOCKING_ENABLED_KEY -> {
                phoneLockingStateRepository.updatePhoneLockingState {
                    it.copy(phoneLockingEnabled = value)
                }
            }
            BATTERY_SYNC_ENABLED_KEY -> {
                batterySyncStateRepository.updateBatterySyncState {
                    it.copy(batterySyncEnabled = value)
                }
                PhoneBatteryComplicationProvider.updateAll(context)
            }
            BATTERY_PHONE_CHARGE_NOTI_KEY ->
                batterySyncStateRepository.updateBatterySyncState {
                    it.copy(phoneChargeNotificationEnabled = value)
                }
            BATTERY_PHONE_LOW_NOTI_KEY ->
                batterySyncStateRepository.updateBatterySyncState {
                    it.copy(phoneLowNotificationEnabled = value)
                }
            DND_SYNC_TO_PHONE_KEY -> {
                dndSyncStateRepository.updateDnDSyncState {
                    it.copy(dndSyncToPhone = value)
                }
                if (value) {
                    startDnDListenerService(context)
                }
            }
            DND_SYNC_WITH_THEATER_KEY -> {
                dndSyncStateRepository.updateDnDSyncState {
                    it.copy(dndSyncWithTheater = value)
                }
                if (value) {
                    startDnDListenerService(context)
                }
            }
            PHONE_SEPARATION_NOTI_KEY -> {
                context.extensionSettingsStore.updateData {
                    it.copy(
                        phoneSeparationNotis = value
                    )
                }
                if (value) {
                    SeparationObserverService.start(context)
                }
            }
        }

    }

    private fun startDnDListenerService(context: Context) {
        Intent(context, LocalDnDAndTheaterCollectorService::class.java).also {
            ContextCompat.startForegroundService(context, it)
        }
    }
}

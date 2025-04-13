package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import com.boswelja.smartwatchextensions.batterysync.platform.PhoneBatteryComplicationProvider
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.dndsync.DnDSyncStateRepository
import com.boswelja.smartwatchextensions.dndsync.LocalDnDAndTheaterCollectorService
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingSettingKeys.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingStateRepository
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [BoolSetting].
 */
class BoolSettingChangeReceiver : WearableListenerService(), KoinComponent {

    private val batterySyncConfigRepository: BatterySyncConfigRepository by inject()
    private val dndSyncStateRepository: DnDSyncStateRepository by inject()
    private val phoneLockingStateRepository: PhoneLockingStateRepository by inject()

    override fun onMessageReceived(message: MessageEvent) {
        if (message.path == UpdateBoolSetting) {
            val pref = BoolSettingSerializer.deserialize(message.data)
            runBlocking { handleBoolPreferenceChange(pref.key, pref.value) }
        }
    }

    private suspend fun handleBoolPreferenceChange(
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
                batterySyncConfigRepository.updateBatterySyncState {
                    it.copy(batterySyncEnabled = value)
                }
                PhoneBatteryComplicationProvider.updateAll(this)
            }
            BATTERY_PHONE_CHARGE_NOTI_KEY ->
                batterySyncConfigRepository.updateBatterySyncState {
                    it.copy(phoneChargeNotificationEnabled = value)
                }
            BATTERY_PHONE_LOW_NOTI_KEY ->
                batterySyncConfigRepository.updateBatterySyncState {
                    it.copy(phoneLowNotificationEnabled = value)
                }
            DND_SYNC_TO_PHONE_KEY -> {
                dndSyncStateRepository.updateDnDSyncState {
                    it.copy(dndSyncToPhone = value)
                }
                if (value) {
                    startDnDListenerService(this)
                }
            }
            DND_SYNC_WITH_THEATER_KEY -> {
                dndSyncStateRepository.updateDnDSyncState {
                    it.copy(dndSyncWithTheater = value)
                }
                if (value) {
                    startDnDListenerService(this)
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

package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Intent
import androidx.core.content.ContextCompat
import com.boswelja.smartwatchextensions.batterysync.PhoneBatteryComplicationProvider
import com.boswelja.smartwatchextensions.common.connection.Messages
import com.boswelja.smartwatchextensions.common.connection.Preference.fromByteArray
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.dndsync.DnDLocalChangeListener
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking

class PreferenceChangeReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        when (messageEvent?.path) {
            Messages.UPDATE_BOOL_PREFERENCE -> {
                val preference = fromByteArray<Boolean>(messageEvent.data)
                handleBoolPreferenceChange(preference.first, preference.second)
            }
            Messages.UPDATE_INT_PREFERENCE -> {
                val preference = fromByteArray<Int>(messageEvent.data)
                handleIntPreferenceChange(preference.first, preference.second)
            }
        }
    }

    private fun handleIntPreferenceChange(key: String, value: Int) {
        runBlocking {
            extensionSettingsStore.updateData {
                var batteryLowThreshold = it.batteryLowThreshold
                var batteryChargeThreshold = it.batteryChargeThreshold
                when (key) {
                    PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY ->
                        batteryChargeThreshold = value
                    PreferenceKey.BATTERY_LOW_THRESHOLD_KEY ->
                        batteryLowThreshold = value
                }
                it.copy(
                    batteryLowThreshold = batteryLowThreshold,
                    batteryChargeThreshold = batteryChargeThreshold
                )
            }
        }
    }

    private fun handleBoolPreferenceChange(key: String, value: Boolean) {
        runBlocking {
            extensionSettingsStore.updateData {
                var batterySyncEnabled = it.batterySyncEnabled
                var phoneLockingEnabled = it.phoneLockingEnabled
                var phoneChargeNotiEnabled = it.phoneChargeNotiEnabled
                var phoneLowNotiEnabled = it.phoneLowNotiEnabled
                var dndSyncToPhone = it.dndSyncToPhone
                var dndSyncWithTheater = it.dndSyncWithTheater
                var phoneSeparationNotis = it.phoneSeparationNotis

                when (key) {
                    PreferenceKey.PHONE_LOCKING_ENABLED_KEY ->
                        phoneLockingEnabled = value
                    PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                        batterySyncEnabled = value
                        PhoneBatteryComplicationProvider
                            .updateAll(this@PreferenceChangeReceiver)
                    }
                    PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY ->
                        phoneChargeNotiEnabled = value
                    PreferenceKey.BATTERY_PHONE_LOW_NOTI_KEY ->
                        phoneLowNotiEnabled = value
                    PreferenceKey.DND_SYNC_TO_PHONE_KEY -> {
                        dndSyncToPhone = value
                        if (dndSyncToPhone) {
                            startDnDListenerService()
                        }
                    }
                    PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
                        dndSyncWithTheater = value
                        if (dndSyncWithTheater) {
                            startDnDListenerService()
                        }
                    }
                    PreferenceKey.PHONE_SEPARATION_NOTI_KEY -> {
                        phoneSeparationNotis = value
                        if (phoneSeparationNotis) {
                            SeparationObserverService.start(this@PreferenceChangeReceiver)
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
    }

    private fun startDnDListenerService() {
        Intent(this, DnDLocalChangeListener::class.java).also {
            ContextCompat.startForegroundService(this, it)
        }
    }
}

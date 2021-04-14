package com.boswelja.devicemanager.phoneconnectionmanager

import android.content.Intent
import androidx.core.content.ContextCompat
import com.boswelja.devicemanager.batterysync.PhoneBatteryComplicationProvider
import com.boswelja.devicemanager.common.preference.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeListener
import com.boswelja.devicemanager.extensions.extensionSettingsStore
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking

class PreferenceChangeReceiver : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        if (dataEvents != null) {
            val currentNodeId = Tasks.await(Wearable.getNodeClient(this).localNode).id
            for (event in dataEvents) {
                if (event.dataItem.uri.toString().endsWith(currentNodeId)) {
                    handlePreferenceChange(event)
                }
            }
        }
    }

    private fun handlePreferenceChange(event: DataEvent) {
        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
        if (!dataMap.isEmpty) {
            runBlocking {
                extensionSettingsStore.updateData {
                    var batterySyncEnabled = it.batterySyncEnabled
                    var phoneLockingEnabled = it.phoneLockingEnabled
                    var phoneChargeNotiEnabled = it.phoneChargeNotiEnabled
                    var phoneLowNotiEnabled = it.phoneLowNotiEnabled
                    var batteryLowThreshold = it.batteryLowThreshold
                    var batteryChargeThreshold = it.batteryChargeThreshold
                    var dndSyncToPhone = it.dndSyncToPhone
                    var dndSyncWithTheater = it.dndSyncWithTheater

                    dataMap.keySet().forEach { key ->
                        when (key) {
                            PreferenceKey.PHONE_LOCKING_ENABLED_KEY ->
                                phoneLockingEnabled = dataMap.getBoolean(key)
                            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                                batterySyncEnabled = dataMap.getBoolean(key)
                                PhoneBatteryComplicationProvider
                                    .updateAll(this@PreferenceChangeReceiver)
                            }
                            PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY ->
                                phoneChargeNotiEnabled = dataMap.getBoolean(key)
                            PreferenceKey.BATTERY_PHONE_LOW_NOTI_KEY ->
                                phoneLowNotiEnabled = dataMap.getBoolean(key)
                            PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY ->
                                batteryChargeThreshold = dataMap.getInt(key)
                            PreferenceKey.DND_SYNC_TO_PHONE_KEY -> {
                                dndSyncToPhone = dataMap.getBoolean(key)
                                if (dndSyncToPhone) {
                                    startDnDListenerService()
                                }
                            }
                            PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
                                dndSyncWithTheater = dataMap.getBoolean(key)
                                if (dndSyncWithTheater) {
                                    startDnDListenerService()
                                }
                            }
                        }
                    }

                    it.copy(
                        phoneLockingEnabled,
                        batterySyncEnabled,
                        phoneChargeNotiEnabled,
                        phoneLowNotiEnabled,
                        batteryLowThreshold,
                        batteryChargeThreshold,
                        dndSyncToPhone,
                        dndSyncWithTheater
                    )
                }
            }
        }
    }

    private fun startDnDListenerService() {
        Intent(this, DnDLocalChangeListener::class.java).also {
            ContextCompat.startForegroundService(this, it)
        }
    }
}

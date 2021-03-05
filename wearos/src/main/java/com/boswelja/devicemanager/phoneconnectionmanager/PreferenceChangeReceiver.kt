package com.boswelja.devicemanager.phoneconnectionmanager

import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.PhoneBatteryComplicationProvider
import com.boswelja.devicemanager.common.preference.PreferenceKey
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.dndsync.DnDLocalChangeListener
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class PreferenceChangeReceiver : WearableListenerService() {

    private var sharedPreferences: SharedPreferences? = null

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
            if (sharedPreferences == null)
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPreferences!!.edit {
                for (key in dataMap.keySet()) {
                    when (key) {
                        in SyncPreferences.BOOL_PREFS -> {
                            val newValue = dataMap.getBoolean(key)
                            putBoolean(key, newValue)
                            onPreferenceChanged(key, newValue)
                        }
                        in SyncPreferences.INT_PREFS -> {
                            val newValue = dataMap.getInt(key)
                            putInt(key, newValue)
                            onPreferenceChanged(key, newValue)
                        }
                    }
                }
            }
        }
    }

    private fun onPreferenceChanged(key: String, newValue: Any) {
        when (key) {
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                if (newValue == false) {
                    sharedPreferences!!.edit().remove(PreferenceKey.BATTERY_PERCENT_KEY).apply()
                    PhoneBatteryComplicationProvider.updateAll(this)
                }
            }
            PreferenceKey.DND_SYNC_TO_PHONE_KEY, PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
                if (newValue == true) {
                    Intent(this, DnDLocalChangeListener::class.java).also {
                        ContextCompat.startForegroundService(this, it)
                    }
                }
            }
        }
    }
}

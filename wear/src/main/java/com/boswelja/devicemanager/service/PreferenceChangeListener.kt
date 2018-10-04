package com.boswelja.devicemanager.service

import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import com.boswelja.devicemanager.common.DnDHandler
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class PreferenceChangeListener : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        super.onDataChanged(dataEvents)
        Log.d("PreferenceChangeListener", "Received change")
        dataEvents?.forEach { event ->
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

            val dndSyncEnabled = dataMap.getBoolean(References.DND_SYNC_ENABLED_PATH)
            // Inverted because when phone is sending, watch is receiving
            val dndReceiving = dataMap.getBoolean(References.DND_SYNC_SEND_PATH)
            val dndSending = dataMap.getBoolean(References.DND_SYNC_RECEIVE_PATH)

            val lockPhoneEnabled = dataMap.getBoolean(References.LOCK_PHONE_PATH)

            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit()
                    .putBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, dndSyncEnabled)
                    .putBoolean(PreferenceKey.DND_SYNC_SEND_KEY, dndSending)
                    .putBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, dndReceiving)
                    .putBoolean(PreferenceKey.LOCK_PHONE_ENABLED, lockPhoneEnabled)
                    .apply()

            if (dndSyncEnabled && dndSending) {
                Log.d("PreferenceChangeListener", "Starting service")
                val intent = Intent(applicationContext, DnDHandler::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
            Log.d("PreferenceChangeListener", "Prefs updated")
        }
    }
}
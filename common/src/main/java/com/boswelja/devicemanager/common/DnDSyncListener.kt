/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.app.NotificationManager
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

/**
 * Receives changes in DnD state
 */
class DnDSyncListener : WearableListenerService() {

    private val tag = "DnDSyncListener"

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val dndReceiving = prefs.getBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, false)
        if (dndReceiving) {
            dataEventBuffer.forEach { event ->
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val changedByUID = dataMap.getString(References.NEW_DND_STATE_CHANGED_BY_PATH)
                    if (changedByUID != CommonUtils.getUID(this)) {
                        val notificationManager = getSystemService(NotificationManager::class.java)
                        val dndEnabled = dataMap.getBoolean(References.NEW_DND_STATE_PATH)
                        if (dndEnabled) {
                            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                        } else {
                            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                        }
                    } else {
                        Log.d(tag, "Change triggered by this device, skipping")
                    }
                }
            }
        } else {
            Log.d(tag, "Device currently not receiving DnD changes")
        }
    }
}
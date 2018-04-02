/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DnDSyncListener : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer?) {
        super.onDataChanged(dataEventBuffer)
        Log.d("DnDSyncListener", "Received DnD Change")
        for (event: DataEvent in dataEventBuffer!!) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val dndEnabled = dataMap.getBoolean("com.boswelja.devicemanager.dndenabled")
                val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    if (dndEnabled) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                    } else {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                } else {
                    Toast.makeText(this, "You need to grant permission via ADB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
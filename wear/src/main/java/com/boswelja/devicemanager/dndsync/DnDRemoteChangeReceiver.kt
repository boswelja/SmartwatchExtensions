/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.dndsync.References
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DnDRemoteChangeReceiver : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (sharedPreferences.getBoolean(PreferenceKey.DND_SYNC_TO_WATCH_KEY, false)) {
            val dataEvent = dataEventBuffer.last()
            if (dataEvent.type == DataEvent.TYPE_CHANGED) {
                val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
                val interruptFilterEnabled = dataMap.getBoolean(References.NEW_DND_STATE_KEY)
                Utils.setInterruptionFilter(this, interruptFilterEnabled)
            }
            dataEventBuffer.release()
        }
    }
}

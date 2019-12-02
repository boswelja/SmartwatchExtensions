/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.interruptfiltersync.References
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DnDSyncRemoteChangeReceiver : WearableListenerService() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (isReceiving()) {
            val dataEvent = dataEventBuffer.last()
            if (dataEvent.type == DataEvent.TYPE_CHANGED) {
                val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
                val interruptFilterEnabled = dataMap.getBoolean(References.NEW_DND_STATE_KEY)
                setInterruptionFilter(interruptFilterEnabled)
            }
            dataEventBuffer.release()
        }
    }

    private fun isReceiving(): Boolean =
            sharedPreferences.getBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false) ||
                    sharedPreferences.getBoolean(PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY, false)

    private fun setInterruptionFilter(interruptFilterEnabled: Boolean) {
        Utils.setInterruptionFilter(this, interruptFilterEnabled)
    }
}

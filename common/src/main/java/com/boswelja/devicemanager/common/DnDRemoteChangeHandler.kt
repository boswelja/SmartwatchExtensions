/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import androidx.preference.PreferenceManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

/**
 * Receives changes in DnD state
 */
class DnDRemoteChangeHandler : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val isPhone = resources.getBoolean(R.bool.device_is_phone)
        val receivingKey = if (isPhone) {
            PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY
        } else {
            PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY
        }

        val dndReceiving = prefs.getBoolean(receivingKey, false)
        if (dndReceiving ||
                (isPhone && prefs.getBoolean(PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY, false))) {
            val dataEvent = dataEventBuffer.last()
            if (dataEvent.type == DataEvent.TYPE_CHANGED) {
                val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
                val dndEnabled = dataMap.getBoolean(References.NEW_DND_STATE_KEY)
                Compat.setInterruptionFilter(this, dndEnabled)
            }
            dataEventBuffer.release()
        }
    }
}
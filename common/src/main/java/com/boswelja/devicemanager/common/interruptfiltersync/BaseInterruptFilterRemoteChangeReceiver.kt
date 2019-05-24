/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.interruptfiltersync

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.interruptfiltersync.InterruptFilterSyncReferences.NEW_INTERRUPT_FILTER_STATE_KEY
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

/**
 * Receives changes in DnD state
 */
abstract class BaseInterruptFilterRemoteChangeReceiver : WearableListenerService() {

    lateinit var sharedPreferences: SharedPreferences

    abstract fun isReceiving(): Boolean

    abstract fun setInterruptionFilter(interruptFilterEnabled: Boolean)

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (isReceiving()) {
            val dataEvent = dataEventBuffer.last()
            if (dataEvent.type == DataEvent.TYPE_CHANGED) {
                val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
                val interruptFilterEnabled = dataMap.getBoolean(NEW_INTERRUPT_FILTER_STATE_KEY)
                setInterruptionFilter(interruptFilterEnabled)
            }
            dataEventBuffer.release()
        }
    }
}

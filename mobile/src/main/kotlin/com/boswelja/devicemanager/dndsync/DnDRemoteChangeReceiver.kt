/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Compat.setInterruptionFilter
import com.boswelja.devicemanager.common.dndsync.References.NEW_DND_STATE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import timber.log.Timber

class DnDRemoteChangeReceiver : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        val watchDndStates = ArrayList<Boolean>()
        for (dataEvent in dataEventBuffer) {
            val dndEnabled = getDnDStateFromDataEvent(dataEvent)
            watchDndStates.add(dndEnabled)
        }
        val shouldEnableDnD = watchDndStates.any { dndEnabled -> dndEnabled }
        val success = setInterruptionFilter(this, shouldEnableDnD)
        if (!success) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit().putBoolean(PreferenceKey.DND_SYNC_TO_PHONE_KEY, false).apply()
        }
    }

    /**
     * Gets the Do not Disturb state from a [DataEvent].
     * @param dataEvent The [DataEvent] to read from.
     * @return true if Do not Disturb is enabled, false otherwise.
     */
    private fun getDnDStateFromDataEvent(dataEvent: DataEvent): Boolean {
        Timber.i("getDnDStateFromDataEvent() called")
        val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
        return dataMap.getBoolean(NEW_DND_STATE_KEY)
    }
}

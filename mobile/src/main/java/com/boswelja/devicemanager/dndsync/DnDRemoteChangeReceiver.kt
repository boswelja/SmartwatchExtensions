/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import com.boswelja.devicemanager.Utils.setInterruptionFilter
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DnDRemoteChangeReceiver : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        val watchDndStates = ArrayList<Boolean>()
        for (dataEvent in dataEventBuffer) {
            val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
            val dndEnabled = dataMap.getBoolean(NEW_DND_STATE_KEY)
            watchDndStates.add(dndEnabled)
        }
        val shouldEnableDnD = watchDndStates.any { dndEnabled -> dndEnabled }
        setInterruptionFilter(this, shouldEnableDnD)
    }

    companion object {
        const val NEW_DND_STATE_KEY = "${References.packageName}.dnd-enabled"
    }
}

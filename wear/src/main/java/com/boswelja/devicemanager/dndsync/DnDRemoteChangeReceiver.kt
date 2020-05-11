/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.dndsync.References
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.boswelja.devicemanager.preferencesync.PreferenceSyncHelper
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DnDRemoteChangeReceiver : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (sharedPreferences.getBoolean(DND_SYNC_TO_WATCH_KEY, false)) {
            val dataEvent = dataEventBuffer.last()
            if (dataEvent.type == DataEvent.TYPE_CHANGED) {
                val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
                val interruptFilterEnabled = dataMap.getBoolean(References.NEW_DND_STATE_KEY)
                val success = Compat.setInterruptionFilter(this, interruptFilterEnabled)
                if (!success) {
                    sharedPreferences.edit(commit = true) {
                        putBoolean(DND_SYNC_TO_WATCH_KEY, false)
                    }
                    PreferenceSyncHelper(
                            this,
                            sharedPreferences,
                            sharedPreferences.getString(PHONE_ID_KEY, "") ?: "").also {
                        it.pushNewData(DND_SYNC_TO_WATCH_KEY)
                    }
                }
            }
            dataEventBuffer.release()
        }
    }
}

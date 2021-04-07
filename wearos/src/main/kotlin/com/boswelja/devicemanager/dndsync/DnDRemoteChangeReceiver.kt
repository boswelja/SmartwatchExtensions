package com.boswelja.devicemanager.dndsync

import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.dndsync.References
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.phoneStateStore
import com.boswelja.devicemanager.phoneconnectionmanager.PreferenceSyncHelper
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class DnDRemoteChangeReceiver : WearableListenerService() {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)
        val dataEvent = dataEventBuffer.last()
        if (dataEvent.type == DataEvent.TYPE_CHANGED) {
            val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
            val interruptFilterEnabled = dataMap.getBoolean(References.NEW_DND_STATE_KEY)
            val success = Compat.setInterruptionFilter(this, interruptFilterEnabled)
            if (!success) {
                val phoneId = runBlocking { phoneStateStore.data.map { it.id }.first() }
                PreferenceSyncHelper(
                    this,
                    phoneId
                ).also { it.pushData(DND_SYNC_TO_WATCH_KEY, false) }
            }
        }
        dataEventBuffer.release()
    }
}

package com.boswelja.devicemanager.phoneconnectionmanager

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class PreferenceSyncHelper(
    context: Context,
    private val phoneId: String
) {

    private val dataClient = Wearable.getDataClient(context)

    fun pushData(key: String, value: Boolean): Task<DataItem>? {
        if (phoneId.isNotEmpty()) {
            val syncedPrefUpdateReq = PutDataMapRequest.create(PREFERENCE_CHANGE_PATH)
            syncedPrefUpdateReq.dataMap.putBoolean(key, value)
            syncedPrefUpdateReq.setUrgent()
            return dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
        }
        return null
    }

    companion object {
        private const val PREFERENCE_CHANGE_PATH = "/preference_change"
    }
}

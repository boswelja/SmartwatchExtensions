package com.boswelja.devicemanager.common.dndsync

import android.content.Context
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.dndsync.References.DND_STATUS_PATH
import com.boswelja.devicemanager.common.dndsync.References.NEW_DND_STATE_KEY
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object Utils {

    /** Ensure Interruption Filter state is properly synced between devices. */
    @Deprecated("Use respective connection interfaces instead")
    fun updateInterruptionFilter(context: Context) {
        val interruptionFilterEnabled = Compat.isDndEnabled(context)
        updateInterruptionFilter(context, interruptionFilterEnabled)
    }

    /**
     * Sets a new Interruption Filter state across devices.
     * @param interruptionFilterEnabled Whether Interruption Filter should be enabled.
     */
    @Deprecated("Use respective connection interfaces instead")
    fun updateInterruptionFilter(context: Context, interruptionFilterEnabled: Boolean) {
        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create(DND_STATUS_PATH)
        putDataMapReq.dataMap.putBoolean(NEW_DND_STATE_KEY, interruptionFilterEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }
}

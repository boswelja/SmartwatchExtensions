package com.boswelja.devicemanager.common.interruptfiltersync

import android.content.Context
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.interruptfiltersync.InterruptFilterSyncReferences.INTERRUPT_FILTER_STATUS_PATH
import com.boswelja.devicemanager.common.interruptfiltersync.InterruptFilterSyncReferences.NEW_INTERRUPT_FILTER_STATE_KEY
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object InterruptFilterSyncUtils {

    /**
     * Ensure Interruption Filter state is properly synced between devices.
     */
    fun updateInterruptionFilter(context: Context) {
        val interruptionFilterEnabled = Compat.interruptionFilterEnabled(context)
        updateInterruptionFilter(context, interruptionFilterEnabled)
    }

    /**
     * Sets a new Interruption Filter state across devices.
     * @param interruptionFilterEnabled Whether Interruption Filter should be enabled.
     */
    fun updateInterruptionFilter(context: Context, interruptionFilterEnabled: Boolean) {
        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create(INTERRUPT_FILTER_STATUS_PATH)
        putDataMapReq.dataMap.putBoolean(NEW_INTERRUPT_FILTER_STATE_KEY, interruptionFilterEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }
}
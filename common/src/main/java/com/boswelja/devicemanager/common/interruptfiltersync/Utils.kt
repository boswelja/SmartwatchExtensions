/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.interruptfiltersync

import android.content.Context
import android.util.Log
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.interruptfiltersync.References.DND_STATUS_PATH
import com.boswelja.devicemanager.common.interruptfiltersync.References.NEW_DND_STATE_KEY
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object Utils {

    /**
     * Ensure Interruption Filter state is properly synced between devices.
     */
    fun updateInterruptionFilter(context: Context) {
        val interruptionFilterEnabled = Compat.interruptionFilterEnabled(context)
        Log.d("updateInterruptFilter", interruptionFilterEnabled.toString())
        updateInterruptionFilter(context, interruptionFilterEnabled)
    }

    /**
     * Sets a new Interruption Filter state across devices.
     * @param interruptionFilterEnabled Whether Interruption Filter should be enabled.
     */
    fun updateInterruptionFilter(context: Context, interruptionFilterEnabled: Boolean) {
        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create(DND_STATUS_PATH)
        putDataMapReq.dataMap.putBoolean(NEW_DND_STATE_KEY, interruptionFilterEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }
}

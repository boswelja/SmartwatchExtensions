package com.boswelja.devicemanager.dndsync

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings
import com.boswelja.devicemanager.common.dndsync.References
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class TheaterModeObserver(private val context: Context, handler: Handler) :
    ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        val isTheaterModeOn = isTheaterModeOn(context)
        updateInterruptionFilter(context, isTheaterModeOn)
    }

    private fun isTheaterModeOn(context: Context): Boolean =
        Settings.Global.getInt(context.contentResolver, "theater_mode_on", 0) == 1

    /**
     * Sets a new Interruption Filter state across devices.
     * @param interruptionFilterEnabled Whether Interruption Filter should be enabled.
     */
    private fun updateInterruptionFilter(context: Context, interruptionFilterEnabled: Boolean) {
        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create(References.DND_STATUS_PATH)
        putDataMapReq.dataMap.putBoolean(References.NEW_DND_STATE_KEY, interruptionFilterEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }
}

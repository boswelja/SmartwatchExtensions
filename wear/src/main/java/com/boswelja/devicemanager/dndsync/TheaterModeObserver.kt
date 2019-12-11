package com.boswelja.devicemanager.dndsync

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings
import com.boswelja.devicemanager.common.dndsync.Utils

class TheaterModeObserver(private val context: Context, handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        val isTheaterModeOn = isTheaterModeOn(context)
        Utils.updateInterruptionFilter(context, isTheaterModeOn)
    }

    private fun isTheaterModeOn(context: Context): Boolean =
            Settings.Global.getInt(context.contentResolver, "theater_mode_on", 0) == 1
}
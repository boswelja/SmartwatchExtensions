/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
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

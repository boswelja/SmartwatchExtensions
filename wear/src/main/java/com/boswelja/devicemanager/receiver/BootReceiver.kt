/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.Context
import android.content.Intent
import com.boswelja.devicemanager.common.BaseBootReceiver
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.service.InterruptFilterLocalChangeListener
import com.boswelja.devicemanager.service.InterruptFilterSyncWithTheaterListener

class BootReceiver : BaseBootReceiver() {

    override fun onBootCompleted(context: Context?) {
        if (sharedPreferences.getBoolean(INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false)){
            startInterruptFilterLocalChangeListener(context)
        }
        if (sharedPreferences.getBoolean(INTERRUPT_FILTER_ON_WITH_THEATER_KEY, false)) {
            startInterruptFilterSyncWithTheaterListener(context)
        }
    }

    private fun startInterruptFilterLocalChangeListener(context: Context?) {
        val intent = Intent(context, InterruptFilterLocalChangeListener::class.java)
        Compat.startForegroundService(context!!, intent)
    }

    private fun startInterruptFilterSyncWithTheaterListener(context: Context?) {
        val intent = Intent(context, InterruptFilterSyncWithTheaterListener::class.java)
        Compat.startForegroundService(context!!, intent)
    }
}

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
import com.boswelja.devicemanager.BatteryUpdateJob
import com.boswelja.devicemanager.common.BaseBootReceiver
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.service.DnDLocalChangeListener

class BootReceiver : BaseBootReceiver() {

    override fun onBootCompleted(context: Context?) {
        if (sharedPreferences.getBoolean(INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false)) {
            startInterruptFilterSyncService(context)
        }
        if (sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)) {
            BatteryUpdateJob.startJob(context!!)
        }
    }

    private fun startInterruptFilterSyncService(context: Context?) {
        val intent = Intent(context, DnDLocalChangeListener::class.java)
        Compat.startForegroundService(context!!, intent)
    }
}

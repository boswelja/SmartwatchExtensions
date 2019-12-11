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
import com.boswelja.devicemanager.batterysync.BatterySyncJob
import com.boswelja.devicemanager.common.BaseBootReceiver
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService

class BootReceiver : BaseBootReceiver() {

    private val watchConnectionmanagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            BatterySyncJob.startJob(service)
            service.unbindService(this)
        }

        override fun onWatchManagerUnbound() {} // Do nothing
    }

    override fun onBootCompleted(context: Context?) {
        if (sharedPreferences.getBoolean(DND_SYNC_TO_WATCH_KEY, false)) {
            startInterruptFilterSyncService(context)
        }
        if (sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)) {
            WatchConnectionService.bind(context!!, watchConnectionmanagerConnection)
        }
    }

    private fun startInterruptFilterSyncService(context: Context?) {
        val intent = Intent(context, DnDLocalChangeService::class.java)
        Compat.startForegroundService(context!!, intent)
    }
}

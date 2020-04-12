/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.Context
import android.content.Intent
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.common.BaseBootReceiver
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BootReceiver : BaseBootReceiver() {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            MainScope().launch {
                withContext(Dispatchers.IO) {
                    tryStartInterruptFilterSyncService(service)
                    tryStartBatterySyncWorkers(service)
                }
                unbindService(service)
            }
        }

        override fun onWatchManagerUnbound() {}
    }

    override fun onBootCompleted(context: Context?) {
        WatchConnectionService.bind(context!!.applicationContext, watchConnectionManagerConnection)
    }

    private suspend fun tryStartInterruptFilterSyncService(service: WatchConnectionService) {
        val dndSyncToWatchEnabled = service
                .getBoolPrefsForRegisteredWatches(DND_SYNC_TO_WATCH_KEY)
                ?.any { it.value } == true
        if (dndSyncToWatchEnabled) {
            service.applicationContext.startService(
                    Intent(service.applicationContext, DnDLocalChangeService::class.java))
        }
    }

    private suspend fun tryStartBatterySyncWorkers(service: WatchConnectionService) {
        val watchBatterySyncInfo = service
                .getBoolPrefsForRegisteredWatches(BATTERY_SYNC_ENABLED_KEY)
                ?.filter { it.value }
        if (watchBatterySyncInfo != null && watchBatterySyncInfo.isNotEmpty()) {
            for (watchBatterySync in watchBatterySyncInfo) {
                val batterySyncInterval = service
                        .getIntPrefForWatch(watchBatterySync.watchId, BATTERY_CHARGE_THRESHOLD_KEY)
                        ?.value?.toLong() ?: 15
                BatterySyncWorker.startWorker(service.applicationContext, watchBatterySync.watchId, batterySyncInterval)
            }
        }
    }

    private fun unbindService(context: Context) {
        context.applicationContext.unbindService(watchConnectionManagerConnection)
    }
}

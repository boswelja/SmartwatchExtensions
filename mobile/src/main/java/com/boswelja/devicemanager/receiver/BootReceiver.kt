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
import android.util.Log
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

    private lateinit var pendingResult: PendingResult

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            Log.i("BootReceiver", "WatchConnectionService Bound")
            MainScope().launch {
                withContext(Dispatchers.IO) {
                    tryStartInterruptFilterSyncService(service)
                    tryStartBatterySyncWorkers(service)
                }
                unbindService(service)
                finish()
            }
        }

        override fun onWatchManagerUnbound() {}
    }

    override fun onBootCompleted(context: Context?) {
        Log.i("BootReceiver", "onBootCompleted called")
        pendingResult = goAsync()
        WatchConnectionService.bind(context!!.applicationContext, watchConnectionManagerConnection)
    }

    private suspend fun tryStartInterruptFilterSyncService(service: WatchConnectionService) {
        Log.i("BootReceiver", "Checking DnD Sync Service")
        val dndSyncToWatchEnabled = service
                .getBoolPrefsForRegisteredWatches(DND_SYNC_TO_WATCH_KEY)
                ?.any { it.value } == true
        if (dndSyncToWatchEnabled) {
            Log.i("BootReceiver", "Starting DnD Sync Service")
            service.applicationContext.startService(
                    Intent(service.applicationContext, DnDLocalChangeService::class.java))
        }
    }

    private suspend fun tryStartBatterySyncWorkers(service: WatchConnectionService) {
        Log.i("BootReceiver", "Checking Battery Sync Workers")
        val watchBatterySyncInfo = service
                .getBoolPrefsForRegisteredWatches(BATTERY_SYNC_ENABLED_KEY)
        if (watchBatterySyncInfo != null && watchBatterySyncInfo.isNotEmpty()) {
            for (batterySyncBoolPreference in watchBatterySyncInfo) {
                if (batterySyncBoolPreference.value) {
                    Log.i("BootReceiver", "Starting Battery Sync Worker")
                    val batterySyncInterval = service
                            .getIntPrefForWatch(batterySyncBoolPreference.watchId, BATTERY_CHARGE_THRESHOLD_KEY)
                            ?.value?.toLong() ?: 15
                    val batterySyncWorkerId = BatterySyncWorker.startWorker(
                            service.applicationContext, batterySyncBoolPreference.watchId, batterySyncInterval)
                    service.updateBatterySyncWorkerId(batterySyncBoolPreference.watchId, batterySyncWorkerId)
                }
            }
        } else {
            Log.i("BootReceiver", "watchBatterySyncInfo null or empty")
        }
    }

    private fun unbindService(context: Context) {
        Log.i("BootReceiver", "WatchConnectionService Unbound")
        context.applicationContext.unbindService(watchConnectionManagerConnection)
    }

    private fun finish() {
        pendingResult.finish()
    }
}

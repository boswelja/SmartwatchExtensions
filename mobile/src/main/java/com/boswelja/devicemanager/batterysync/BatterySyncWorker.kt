/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync

import android.content.Context
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.batterysync.Utils
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class BatterySyncWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Timber.i("doWork() called")
        val watchId = inputData.getString(EXTRA_WATCH_ID)
        if (!watchId.isNullOrEmpty()) {
            Utils.updateBatteryStats(applicationContext, watchId)
            return Result.success()
        } else {
            Timber.w("watchId null or empty")
        }
        return Result.failure()
    }

    companion object {
        private const val EXTRA_WATCH_ID: String = "extra_watch_id"

        /**
         * Starts a battery sync job for the currently selected watch.
         * @return @`true` if the job was started successfully, @`false` otherwise.
         */
        suspend fun startWorker(watchConnectionManager: WatchConnectionService?): String? {
            return withContext(Dispatchers.Default) {
                val connectedWatch = watchConnectionManager?.getConnectedWatch()
                if (connectedWatch != null) {
                    val syncIntervalMinutes = connectedWatch.intPrefs[PreferenceKey.BATTERY_SYNC_INTERVAL_KEY] ?: 15
                    val newWorkerId = startWorker(watchConnectionManager, connectedWatch.id, syncIntervalMinutes.toLong())
                    watchConnectionManager.updateBatterySyncWorkerId(connectedWatch.id, newWorkerId)
                    return@withContext newWorkerId
                }
                null
            }
        }

        fun startWorker(context: Context, watchId: String, syncIntervalMinutes: Long): String {
            val data = Data.Builder().apply {
                putString(EXTRA_WATCH_ID, watchId)
            }.build()
            val request = PeriodicWorkRequestBuilder<BatterySyncWorker>(
                    syncIntervalMinutes, TimeUnit.SECONDS).apply {
                setInputData(data)
            }.build()
            WorkManager.getInstance(context).enqueue(request)
            return request.id.toString()
        }

        /**
         * Stops the battery sync job for the current watch.
         */
        suspend fun stopWorker(watchConnectionManager: WatchConnectionService?) {
            withContext(Dispatchers.IO) {
                val connectedWatch = watchConnectionManager?.getConnectedWatch()
                withContext(Dispatchers.Default) {
                    if (connectedWatch != null) {
                        val workerId = connectedWatch.batterySyncWorkerId
                        if (workerId != null) {
                            stopWorker(watchConnectionManager, workerId)
                        }
                    }
                }
            }
        }

        fun stopWorker(context: Context, workerIdString: String) {
            WorkManager.getInstance(context).cancelWorkById(UUID.fromString(workerIdString))
        }
    }
}

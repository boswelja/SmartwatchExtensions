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
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit

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
         * Starts a [BatterySyncWorker] for the currently selected watch.
         * @return @`true` if the job was started successfully, @`false` otherwise.
         */
        suspend fun startWorker(watchConnectionManager: WatchManager?): Boolean {
            return withContext(Dispatchers.Default) {
                val connectedWatch = watchConnectionManager?.connectedWatch
                if (connectedWatch != null) {
                    val syncIntervalMinutes = connectedWatch.intPrefs[PreferenceKey.BATTERY_SYNC_INTERVAL_KEY] ?: 15
                    val newWorkerId = startWorker(watchConnectionManager, connectedWatch.id, syncIntervalMinutes.toLong())
                    watchConnectionManager.updateBatterySyncWorkerId(connectedWatch.id, newWorkerId)
                    return@withContext true
                }
                return@withContext false
            }
        }

        suspend fun startWorker(watchConnectionManager: WatchManager?, watchId: String): Boolean {
            return withContext(Dispatchers.Default) {
                val watch = watchConnectionManager?.getWatchById(watchId)
                if (watch != null) {
                    val syncIntervalMinutes = watch.intPrefs[PreferenceKey.BATTERY_SYNC_INTERVAL_KEY] ?: 15
                    val newWorkerId = startWorker(watchConnectionManager, watch.id, syncIntervalMinutes.toLong())
                    watchConnectionManager.updateBatterySyncWorkerId(watch.id, newWorkerId)
                    return@withContext true
                }
                return@withContext false
            }
        }

        fun startWorker(context: Context, watchId: String, syncIntervalMinutes: Long): String {
            val data = Data.Builder().apply {
                putString(EXTRA_WATCH_ID, watchId)
            }.build()
            val request = PeriodicWorkRequestBuilder<BatterySyncWorker>(
                syncIntervalMinutes, TimeUnit.SECONDS
            ).apply {
                setInputData(data)
            }.build()
            WorkManager.getInstance(context).enqueue(request)
            return request.id.toString()
        }

        /**
         * Stops the battery sync job for the current watch.
         */
        suspend fun stopWorker(watchConnectionManager: WatchManager?) {
            withContext(Dispatchers.IO) {
                val connectedWatch = watchConnectionManager?.connectedWatch
                withContext(Dispatchers.Default) {
                    if (connectedWatch != null) {
                        val workerId = connectedWatch.batterySyncWorkerId
                        if (workerId != null) {
                            stopWorkerById(watchConnectionManager, workerId)
                        }
                    }
                }
            }
        }

        /**
         * Stops the battery sync job for a watch with the given ID.
         */
        suspend fun stopWorker(watchConnectionManager: WatchManager?, watchId: String) {
            withContext(Dispatchers.IO) {
                val watch = watchConnectionManager?.getWatchById(watchId)
                withContext(Dispatchers.Default) {
                    if (watch != null) {
                        val workerId = watch.batterySyncWorkerId
                        if (workerId != null) {
                            stopWorkerById(watchConnectionManager, workerId)
                        }
                    }
                }
            }
        }

        private fun stopWorkerById(context: Context, workerIdString: String) {
            WorkManager.getInstance(context).cancelWorkById(UUID.fromString(workerIdString))
        }
    }
}

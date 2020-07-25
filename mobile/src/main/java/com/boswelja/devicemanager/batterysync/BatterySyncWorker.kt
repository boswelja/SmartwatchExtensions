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
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.ui.batterysync.Utils
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
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

        suspend fun startWorker(context: Context, watchId: String): Boolean {
            return withContext(Dispatchers.IO) {
                val database = WatchDatabase.get(context)
                val syncIntervalMinutes =
                    (database.intPrefDao().getWhere(watchId, BATTERY_SYNC_INTERVAL_KEY)?.value ?: 15).toLong()
                val data = Data.Builder().apply {
                    putString(EXTRA_WATCH_ID, watchId)
                }.build()
                val request = PeriodicWorkRequestBuilder<BatterySyncWorker>(
                    syncIntervalMinutes, TimeUnit.SECONDS
                ).apply {
                    setInputData(data)
                }.build()
                WorkManager.getInstance(context).enqueue(request)
                val newWorkerId = request.id.toString()
                database.watchDao().updateBatterySyncWorkerId(watchId, newWorkerId)
                return@withContext true
            }
        }

        /**
         * Stops the battery sync job for a watch with the given ID.
         */
        suspend fun stopWorker(context: Context, watchId: String) {
            withContext(Dispatchers.IO) {
                val database = WatchDatabase.get(context)
                val watch = database.watchDao().get(watchId)
                watch?.batterySyncWorkerId?.let {
                    WorkManager.getInstance(context).cancelWorkById(UUID.fromString(it))
                }
            }
        }
    }
}

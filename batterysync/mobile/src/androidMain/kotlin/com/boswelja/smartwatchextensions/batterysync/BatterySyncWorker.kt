package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class BatterySyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val watchId = inputData.getString(EXTRA_WATCH_ID)!!
        if (sendBatteryStats(applicationContext, watchId)) return Result.success()

        return Result.retry()
    }

    companion object {
        private const val SYNC_INTERVAL_MINUTES = 15L
        private const val EXTRA_WATCH_ID: String = "extra_watch_id"

        private fun getWorkerIdFor(watchId: String): String = "$watchId-batterysync"

        /** Starts a battery sync worker for the watch with a given ID. */
        fun startSyncingFor(context: Context, watchId: String): Boolean {
            val data = Data.Builder().putString(EXTRA_WATCH_ID, watchId).build()
            val request = PeriodicWorkRequestBuilder<BatterySyncWorker>(
                SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES,
                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS
            ).apply {
                setInputData(data)
            }.build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                getWorkerIdFor(watchId),
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
            return true
        }

        /** Stops the battery sync worker for the watch with a given ID. */
        fun stopSyncingFor(context: Context, watchId: String) {
            WorkManager.getInstance(context).cancelUniqueWork(getWorkerIdFor(watchId))
        }

        /**
         * Get up to date battery stats for this device and send it to a specified watch, or all watches
         * with battery sync enabled.
         * @param context [Context].
         * @param watchId The ID of the watch to send the stats to.
         */
        suspend fun sendBatteryStats(context: Context, watchId: String): Boolean {
            val batteryStats = context.batteryStats()!!
            TODO("Send the stats")
        }

    }
}

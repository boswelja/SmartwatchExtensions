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

actual abstract class BaseBatterySyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    actual abstract suspend fun onSendBatteryStats(
        targetUid: String,
        batteryStats: BatteryStats
    ): Boolean

    override suspend fun doWork(): Result {
        val watchId = inputData.getString(EXTRA_WATCH_ID)!!
        val batteryStats = applicationContext.batteryStats()!!
        if (onSendBatteryStats(watchId, batteryStats)) return Result.success()

        return Result.retry()
    }

    companion object {
        const val SYNC_INTERVAL_MINUTES = 15L
        const val EXTRA_WATCH_ID: String = "extra_watch_id"

        fun getWorkerIdFor(watchId: String): String = "$watchId-batterysync"

        /** Starts a battery sync worker for the watch with a given ID. */
        inline fun <reified T : BaseBatterySyncWorker> startSyncingFor(context: Context, watchId: String): Boolean {
            val data = Data.Builder().putString(EXTRA_WATCH_ID, watchId).build()
            val request = PeriodicWorkRequestBuilder<T>(
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
    }
}

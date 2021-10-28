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

/**
 * A base [CoroutineWorker] to handle getting battery stats for the device.
 */
abstract class BaseBatterySyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    /**
     * Called when the device battery stats are ready to send. Sending battery stats should be
     * handled here.
     * @param targetUid The target device UID.
     * @param batteryStats The device battery stats to send.
     */
    abstract suspend fun onSendBatteryStats(
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

        /**
         * The worker sync interval in minutes.
         */
        const val SYNC_INTERVAL_MINUTES = 15L

        /**
         * The key for the extra containing the target device UID.
         */
        const val EXTRA_WATCH_ID: String = "extra_watch_id"

        /**
         * Returns a unique worker ID for battery sync with the target UID.
         * @param watchId The target device UID.
         */
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

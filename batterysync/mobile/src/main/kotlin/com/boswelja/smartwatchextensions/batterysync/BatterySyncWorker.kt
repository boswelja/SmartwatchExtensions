package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.workDataOf
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * A [CoroutineWorker] to handle syncing battery stats for a watch.
 */
class BatterySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val messageClient: MessageClient by inject()

    override suspend fun doWork(): Result {
        val watchId = inputData.getString(EXTRA_WATCH_ID) ?: return Result.failure()
        val batteryStats = applicationContext.batteryStats()!!
        return if (onSendBatteryStats(watchId, batteryStats)) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    /**
     * Called when the device battery stats are ready to send. Sending battery stats should be
     * handled here.
     * @param targetUid The target device UID.
     * @param batteryStats The device battery stats to send.
     */
    private suspend fun onSendBatteryStats(
        targetUid: String,
        batteryStats: BatteryStats
    ): Boolean {
        val handler = MessageHandler(BatteryStatsSerializer, messageClient)
        return handler.sendMessage(targetUid, Message(BatteryStatus, batteryStats))
    }

    companion object {

        /**
         * The key for the extra containing the target device UID.
         */
        internal const val EXTRA_WATCH_ID: String = "extra_watch_id"

        /**
         * Returns a unique worker ID for battery sync with the target UID.
         * @param watchId The target device UID.
         */
        private fun getWorkerIdFor(watchId: String): String = "$watchId-batterysync"

        /** Starts a battery sync worker for the watch with a given ID. */
        suspend fun startSyncingFor(context: Context, watchId: String): Boolean {
            val workerId = getWorkerIdFor(watchId)
            // Don't specify flex interval. Seems like the worker doesn't get executed anymore
            val request = PeriodicWorkRequestBuilder<BatterySyncWorker>(
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            ).setInputData(workDataOf(EXTRA_WATCH_ID to watchId))
                .addTag(workerId)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                workerId,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            ).result.await()
            return true
        }

        /** Stops the battery sync worker for the watch with a given ID. */
        fun stopSyncingFor(context: Context, watchId: String) {
            WorkManager.getInstance(context).cancelUniqueWork(getWorkerIdFor(watchId))
        }
    }
}

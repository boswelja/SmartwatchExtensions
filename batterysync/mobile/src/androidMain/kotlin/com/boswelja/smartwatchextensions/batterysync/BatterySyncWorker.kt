package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * A [CoroutineWorker] to handle syncing battery stats for a watch.
 */
class BatterySyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {

    private val watchRepository: WatchRepository by inject()
    private val messageClient: MessageClient by inject()

    override suspend fun doWork(): Result {
        val watchId = inputData.getString(EXTRA_WATCH_ID) ?: return Result.failure()
        val batteryStats = applicationContext.batteryStats() ?: return Result.retry()
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
        return watchRepository.getWatchById(targetUid).firstOrNull()?.let { watch ->
            handler.sendMessage(watch.uid, Message(BATTERY_STATUS_PATH, batteryStats))
        } ?: false
    }

    companion object {

        /**
         * The worker sync interval in minutes.
         */
        private const val SYNC_INTERVAL_MINUTES = 15L

        /**
         * The key for the extra containing the target device UID.
         */
        const val EXTRA_WATCH_ID: String = "extra_watch_id"

        /**
         * Returns a unique worker ID for battery sync with the target UID.
         * @param watchId The target device UID.
         */
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
    }
}

package com.boswelja.devicemanager.batterysync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.database.WatchSettingsDatabase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class BatterySyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.i("doWork() called")
        val watchId = inputData.getString(EXTRA_WATCH_ID)
        WatchManager.getInstance(applicationContext).registeredWatches.value!!.firstOrNull {
            it.id == watchId
        }?.let {
            Utils.updateBatteryStats(applicationContext, it)
            return Result.success()
        }
        Timber.w("watchId null or empty")
        return Result.retry()
    }

    companion object {
        private const val EXTRA_WATCH_ID: String = "extra_watch_id"

        suspend fun startWorker(context: Context, watchId: String): Boolean {
            return withContext(Dispatchers.IO) {
                val database = WatchSettingsDatabase.getInstance(context)
                val syncIntervalMinutes =
                    (database.intPrefDao().get(watchId, BATTERY_SYNC_INTERVAL_KEY)?.value ?: 15)
                        .toLong()
                val data = Data.Builder().apply { putString(EXTRA_WATCH_ID, watchId) }.build()
                val request =
                    PeriodicWorkRequestBuilder<BatterySyncWorker>(
                        syncIntervalMinutes, TimeUnit.SECONDS
                    ).apply { setInputData(data) }.build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "$watchId-batterysync",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
                return@withContext true
            }
        }

        /** Stops the battery sync job for a watch with the given ID. */
        fun stopWorker(context: Context, watchId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("$watchId-batterysync")
        }
    }
}

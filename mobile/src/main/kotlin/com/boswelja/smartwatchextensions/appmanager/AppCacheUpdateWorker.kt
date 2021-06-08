package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.Messages.VALIDATE_CACHE
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * A [CoroutineWorker] designed to validate a given watches app cache.
 */
class AppCacheUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val watchManager = WatchManager.getInstance(applicationContext)
    private val appDatabase = WatchAppDatabase.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        // Get watch ID
        val watchId = UUID.fromString(inputData.getString(EXTRA_WATCH_ID))

        val watch = watchManager.getWatchById(watchId).firstOrNull()
        if (watch != null && validateCacheFor(watch)) {
            return Result.success()
        }

        Timber.w("Failed to validate watch app cache for %s", watchId)
        return Result.retry()
    }

    private suspend fun validateCacheFor(watch: Watch): Boolean {
        Timber.d("Validating cache for %s", watch.id)
        // Get a list of packages we have for the given watch
        val apps = appDatabase.apps().allForWatch(watch.id)
            .map { apps ->
                apps
                    .map { it.packageName }
                    .sorted()
            }
            .first()
        return watchManager.sendMessage(watch, VALIDATE_CACHE, apps.hashCode().toByteArray())
    }

    companion object {
        private const val EXTRA_WATCH_ID: String = "extra_watch_id"

        private fun getWorkerNameFor(watchId: UUID): String {
            return "appcache-$watchId"
        }

        fun enqueueWorkerFor(context: Context, watchId: UUID) {
            // Define work constraints
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Pass in watch ID
            val data = workDataOf(EXTRA_WATCH_ID to watchId.toString())

            // Create a work request
            val request = PeriodicWorkRequestBuilder<AppCacheUpdateWorker>(
                1, TimeUnit.DAYS
            ).setConstraints(constraints)
                .setInputData(data)
                .build()

            // Enqueue the work request
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(
                getWorkerNameFor(watchId),
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }

        fun stopWorkerFor(context: Context, watchId: UUID) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(getWorkerNameFor(watchId))
        }
    }
}

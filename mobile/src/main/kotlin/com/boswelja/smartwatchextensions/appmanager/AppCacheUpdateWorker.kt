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
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * A [CoroutineWorker] designed to validate app cache for the given watch.
 */
class AppCacheUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val watchManager by lazy { WatchManager.getInstance(applicationContext) }
    private val appDatabase by lazy { WatchAppDatabase.getInstance(applicationContext) }

    override suspend fun doWork(): Result {
        val idString = inputData.getString(EXTRA_WATCH_ID)
        if (idString.isNullOrBlank()) {
            Timber.w("No watch ID passed to worker, cancelling")
            return Result.failure()
        }

        // Get watch and try validate cache
        val watch = watchManager.getWatchById(idString).firstOrNull()
        if (watch != null && validateCacheFor(watch)) {
            return Result.success()
        }

        Timber.w("Failed to validate watch app cache for %s", idString)
        return Result.retry()
    }

    /**
     * Requests cache validation for a given watch.
     * @param watch The [Watch] to validate cache for.
     * @return true if sending the validation request succeeded, false otherwise.
     */
    private suspend fun validateCacheFor(watch: Watch): Boolean {
        Timber.d("Validating cache for %s", watch.uid)

        // Get a list of packages we have for the given watch
        val apps = appDatabase.apps().allForWatch(watch.uid)
            .map { apps ->
                apps.map { it.packageName to it.lastUpdateTime }
            }
            .first()

        // Get a hash for our app list and send it
        val cacheHash = CacheValidation.getHashCode(apps)
        return watchManager.sendMessage(
            watch,
            Message(
                VALIDATE_CACHE,
                cacheHash
            )
        )
    }

    companion object {
        const val EXTRA_WATCH_ID: String = "extra_watch_id"

        /**
         * Get a string that can be used to represent an [AppCacheUpdateWorker] for a watch with a
         * given ID.
         * @param watchId The [Watch.uid] of the watch associated with the worker.
         * @return A string unique to the worker defined for the watch with the given ID.
         */
        private fun getWorkerNameFor(watchId: String): String {
            return "appcache-$watchId"
        }

        /**
         * Enqueue a new [AppCacheUpdateWorker] with appropriate constraints for the watch with the
         * given ID.
         * @param context [Context].
         * @param watchId The [Watch.uid] of the watch associated with the worker.
         * @return The ID of the work request.
         */
        fun enqueueWorkerFor(context: Context, watchId: String): UUID {
            // Define work constraints
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Pass in watch ID
            val data = workDataOf(EXTRA_WATCH_ID to watchId)

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

            return request.id
        }

        /**
         * Stops any enqueued [AppCacheUpdateWorker] for the watch with the given ID.
         * @param context [Context].
         * @param watchId The [Watch.uid] of the watch associated with the worker.
         */
        fun stopWorkerFor(context: Context, watchId: String) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(getWorkerNameFor(watchId))
        }
    }
}

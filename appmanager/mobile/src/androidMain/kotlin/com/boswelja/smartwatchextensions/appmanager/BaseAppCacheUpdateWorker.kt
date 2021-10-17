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
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * A [CoroutineWorker] designed to validate app cache for the given watch.
 */
actual abstract class BaseAppCacheUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), DIAware {

    override val di: DI by closestDI(context)

    private val appRepository: WatchAppRepository by instance()

    actual abstract suspend fun onSendCacheState(
        targetUid: String,
        cacheHash: Int
    ): Boolean

    override suspend fun doWork(): Result {
        val watchId = inputData.getString(EXTRA_WATCH_ID)
        if (watchId.isNullOrBlank()) {
            return Result.failure()
        }

        // Get cache hash
        val apps = appRepository.getAppVersionsFor(watchId)
            .map { apps ->
                apps.map { it.packageName to it.updateTime }
            }
            .first()
        val cacheHash = CacheValidation.getHashCode(apps)

        if (onSendCacheState(watchId, cacheHash)) {
            return Result.success()
        }
        return Result.retry()
    }

    companion object {
        const val EXTRA_WATCH_ID: String = "extra_watch_id"

        /**
         * Get a string that can be used to represent an [BaseAppCacheUpdateWorker] for a watch with
         * a given ID.
         * @param watchId The [Watch.uid] of the watch associated with the worker.
         * @return A string unique to the worker defined for the watch with the given ID.
         */
        fun getWorkerNameFor(watchId: String): String {
            return "appcache-$watchId"
        }

        /**
         * Enqueue a new [BaseAppCacheUpdateWorker] with appropriate constraints for the watch with
         * the given ID.
         * @param context [Context].
         * @param watchId The [Watch.uid] of the watch associated with the worker.
         * @return The ID of the work request.
         */
        inline fun <reified T : BaseAppCacheUpdateWorker> enqueueWorkerFor(
            context: Context,
            watchId: String
        ): UUID {
            // Define work constraints
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Pass in watch ID
            val data = workDataOf(EXTRA_WATCH_ID to watchId)

            // Create a work request
            val request = PeriodicWorkRequestBuilder<T>(
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
         * Stops any enqueued [BaseAppCacheUpdateWorker] for the watch with the given ID.
         * @param context [Context].
         * @param watchId The [Watch.uid] of the watch associated with the worker.
         */
        fun stopWorkerFor(context: Context, watchId: String) {
            WorkManager.getInstance(context).cancelUniqueWork(getWorkerNameFor(watchId))
        }
    }
}

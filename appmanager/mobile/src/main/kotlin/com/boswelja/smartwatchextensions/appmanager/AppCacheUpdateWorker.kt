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
import com.boswelja.smartwatchextensions.wearable.ext.sendMessage
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * A [CoroutineWorker] designed to validate app cache for the given watch.
 */
class AppCacheUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val appRepository: WatchAppRepository by inject()
    private val messageClient: MessageClient by inject()

    override suspend fun doWork(): Result {
        val watchId = inputData.getString(EXTRA_WATCH_ID)
        requireNotNull(watchId)

        // Get cache hash
        val appVersions = getCachedVersions(watchId)

        return if (sendCacheState(watchId, appVersions)) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    private suspend fun getCachedVersions(targetUid: String): AppVersions {
        val appVersions = appRepository.getAppVersionsFor(targetUid)
            .map { apps -> apps.map { AppVersion(it.packageName, it.versionCode) } }
            .first()
        return AppVersions(appVersions)
    }

    private suspend fun sendCacheState(targetUid: String, cacheHash: AppVersions): Boolean {
        return messageClient.sendMessage(
            targetId = targetUid,
            RequestValidateCache,
            CacheValidationSerializer.serialize(cacheHash)
        )
    }

    companion object {

        /**
         * The key for the extra containing the target device UID.
         */
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
        fun enqueueWorkerFor(
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
            val request = PeriodicWorkRequestBuilder<AppCacheUpdateWorker>(
                1, TimeUnit.DAYS
            ).setConstraints(constraints)
                .setInputData(data)
                .build()

            // Enqueue the work request
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(
                getWorkerNameFor(watchId),
                ExistingPeriodicWorkPolicy.UPDATE,
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
            WorkManager.getInstance(context).cancelUniqueWork(getWorkerNameFor(watchId))
        }
    }
}

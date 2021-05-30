package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.phoneStateStore
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

class AppManagerWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Get phone ID
        val phoneId = applicationContext.phoneStateStore.data.map { it.id }.firstOrNull()
        if (phoneId.isNullOrBlank()) {
            Timber.w("Couldn't get phone ID, rescheduling")
            return Result.retry()
        }

        try {
            Timber.d("Sending all apps")
            applicationContext.sendAllApps(phoneId)
        } catch (e: Exception) {
            Timber.e(e)
            return Result.retry()
        }

        return Result.success()
    }

    companion object {
        fun enqueueWorker(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<AppManagerWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(request)
        }
    }
}

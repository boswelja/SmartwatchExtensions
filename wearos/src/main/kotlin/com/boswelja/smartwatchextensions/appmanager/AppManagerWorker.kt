package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import android.content.pm.PackageManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.Messages
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.phoneStateStore
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
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

        // Get all apps
        val packageManager = applicationContext.packageManager
        val allApps = packageManager.getAllApps()
        Timber.d("Got %s apps", allApps.count())

        val messageClient = Wearable.getMessageClient(applicationContext)

        try {
            // Send expected app count to the phone
            val data = allApps.count().toByteArray()
            messageClient.sendMessage(
                phoneId,
                Messages.EXPECTED_APP_COUNT,
                data
            ).await()

            // Start sending apps
            allApps.forEach { app ->
                val appBytes = app.toByteArray()
                Timber.d("Sending %s bytes", appBytes.count())
                messageClient.sendMessage(
                    phoneId,
                    Messages.PACKAGE_ADDED,
                    appBytes
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
            return Result.retry()
        }

        return Result.success()
    }

    private fun PackageManager.getAllApps(): List<App> {
        return getInstalledPackages(PackageManager.GET_PERMISSIONS)
            .map {
                App(this, it)
            }
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

package com.boswelja.devicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExperimentalExpeditedWork
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.dndsync.DnDLocalChangeListener
import com.boswelja.devicemanager.extensions.extensionSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class BootHandler : BroadcastReceiver() {

    @ExperimentalExpeditedWork
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("Intent received")
        if (context == null) {
            Timber.w("Context null")
            return
        }
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Timber.d("Handling ${intent.action}")
                val workRequest = OneTimeWorkRequestBuilder<BootWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}

class BootWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        CapabilityUpdater(applicationContext).updateCapabilities()
        withContext(Dispatchers.IO) {
            val dndSyncToPhone = applicationContext.extensionSettingsStore.data
                .map { it.dndSyncToPhone }.first()
            val dndSyncWithTheater = applicationContext.extensionSettingsStore.data
                .map { it.dndSyncWithTheater }.first()
            if (dndSyncToPhone || dndSyncWithTheater) {
                Intent(applicationContext, DnDLocalChangeListener::class.java).also {
                    ContextCompat.startForegroundService(applicationContext, it)
                }
            }
        }
        return Result.success()
    }

    @ExperimentalExpeditedWork
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat
            .Builder(applicationContext, BOOT_OR_UPDATE_NOTI_CHANNEL_ID)
            .setOngoing(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setProgress(0, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(applicationContext.getString(R.string.notification_boot_handler_title))
            .setSmallIcon(R.drawable.noti_ic_update)
            .build()
        return ForegroundInfo(NOTI_ID, notification)
    }

    companion object {
        const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
        const val NOTI_ID = 69102
    }
}

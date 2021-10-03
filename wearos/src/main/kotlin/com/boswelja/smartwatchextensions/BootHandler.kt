package com.boswelja.smartwatchextensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.dndsync.DnDLocalChangeListener
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class BootHandler : BroadcastReceiver() {

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

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
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

    private fun createNotificationChannel() {
        NotificationChannel(
            BOOT_OR_UPDATE_NOTI_CHANNEL_ID,
            applicationContext.getString(R.string.boot_noti_channel_title),
            IMPORTANCE_LOW
        ).apply {
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
        }.also {
            applicationContext.getSystemService<NotificationManager>()
                ?.createNotificationChannel(it)
        }
    }

    companion object {
        const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
        const val NOTI_ID = 69102
    }
}

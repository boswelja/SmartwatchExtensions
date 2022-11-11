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
import com.boswelja.smartwatchextensions.dndsync.DnDSyncStateRepository
import com.boswelja.smartwatchextensions.dndsync.LocalDnDAndTheaterCollectorService
import com.google.android.gms.wearable.CapabilityClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [BroadcastReceiver] for receiving [Intent.ACTION_BOOT_COMPLETED].
 */
class BootHandler : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            return
        }
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val workRequest = OneTimeWorkRequestBuilder<BootWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}

/**
 * A [CoroutineWorker] to restart services as needed.
 */
class BootWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val discoveryClient: CapabilityClient by inject()
    private val dndSyncStateRepository: DnDSyncStateRepository by inject()

    override suspend fun doWork(): Result {
        CapabilityUpdater(applicationContext, discoveryClient).updateCapabilities()
        withContext(Dispatchers.IO) {
            val shouldStartDnDSyncService = dndSyncStateRepository.getDnDSyncState()
                .map { it.dndSyncToPhone || it.dndSyncWithTheater }
                .first()
            if (shouldStartDnDSyncService) {
                Intent(applicationContext, LocalDnDAndTheaterCollectorService::class.java).also {
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
        private const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
        private const val NOTI_ID = 69102
    }
}

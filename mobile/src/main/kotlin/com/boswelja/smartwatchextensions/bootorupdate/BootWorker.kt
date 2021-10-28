package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [CoroutineWorker] to be called when the device reboots. This is responsible for restarting any
 * services needed for the user's desired operation.
 */
class BootWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val settingsRepository: WatchSettingsRepository by inject()

    override suspend fun doWork(): Result {
        applicationContext.restartServices(settingsRepository)
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        NotificationChannelHelper.createForBootOrUpdate(
            applicationContext, applicationContext.getSystemService()!!
        )
        val notification = NotificationCompat.Builder(
            applicationContext, BOOT_OR_UPDATE_NOTI_CHANNEL_ID
        )
            .setOngoing(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setProgress(0, 0, true)
            .setContentTitle(applicationContext.getString(R.string.notification_boot_handler_title))
            .setSmallIcon(R.drawable.noti_ic_update)
            .build()
        return ForegroundInfo(NOTI_ID, notification)
    }
}

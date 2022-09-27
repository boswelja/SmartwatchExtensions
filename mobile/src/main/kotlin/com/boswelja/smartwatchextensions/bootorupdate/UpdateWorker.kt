package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [CoroutineWorker] to be launched when the app is updated. This is responsible for handling any
 * post-update migrations if needed.
 */
class UpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val settingsRepository: WatchSettingsRepository by inject()

    override suspend fun doWork(): Result {
        // Restart services
        applicationContext.restartServices(settingsRepository)

        return success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        NotificationChannelHelper.createForBootOrUpdate(
            applicationContext, applicationContext.getSystemService()!!
        )
        val notification = NotificationCompat.Builder(
            applicationContext, BootUpdateNotiChannelId
        )
            .setOngoing(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setProgress(0, 0, true)
            .setContentTitle(
                applicationContext.getString(R.string.notification_update_handler_title)
            )
            .setSmallIcon(R.drawable.noti_ic_update)
            .build()
        return ForegroundInfo(NotiId, notification)
    }
}

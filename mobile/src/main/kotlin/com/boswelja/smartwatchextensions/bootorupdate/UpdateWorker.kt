package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ExperimentalExpeditedWork
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R

class UpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Perform any pending updates
        val updater = Updater(applicationContext)
        val result = updater.migrate()

        // Restart services
        applicationContext.restartServices()

        return if (result) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    @ExperimentalExpeditedWork
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
            .setContentTitle(
                applicationContext.getString(R.string.notification_update_handler_title)
            )
            .setSmallIcon(R.drawable.noti_ic_update)
            .build()
        return ForegroundInfo(NOTI_ID, notification)
    }
}

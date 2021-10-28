package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker.Result.failure
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.messages.sendMessage
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
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

    private val messagesRepository: MessagesRepository by inject()
    private val settingsRepository: WatchSettingsRepository by inject()

    override suspend fun doWork(): Result {
        // Perform any pending updates
        val updater = Updater(applicationContext)
        val result = updater.migrate()

        // Restart services
        applicationContext.restartServices(settingsRepository)

        return when (result) {
            com.boswelja.migration.Result.SUCCESS,
            com.boswelja.migration.Result.NOT_NEEDED -> {
                val message = Message(
                    Message.Icon.UPDATE,
                    applicationContext.getString(R.string.update_completed_title),
                    applicationContext.getString(
                        R.string.update_complete_text, BuildConfig.VERSION_NAME
                    )
                )
                applicationContext.sendMessage(message, repository = messagesRepository)
                success()
            }
            com.boswelja.migration.Result.FAILED -> failure()
        }
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
            .setContentTitle(
                applicationContext.getString(R.string.notification_update_handler_title)
            )
            .setSmallIcon(R.drawable.noti_ic_update)
            .build()
        return ForegroundInfo(NOTI_ID, notification)
    }
}

package com.boswelja.smartwatchextensions.updatechecker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.messages.Priority
import com.boswelja.smartwatchextensions.messages.sendMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * A [CoroutineWorker] to handle checking for updates and notifying the user.
 */
class UpdateCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val messagesRepository: MessagesRepository by inject()

    override suspend fun doWork(): Result {
        val updater = GooglePlayUpdateChecker(applicationContext)
        if (updater.isNewVersionAvailable()) {
            val message = Message(
                icon = Message.Icon.UPDATE,
                title = applicationContext.getString(R.string.update_available_title),
                text = applicationContext.getString(R.string.update_available_text),
                action = Message.Action.INSTALL_UPDATE
            )
            applicationContext.sendMessage(message, Priority.HIGH, repository = messagesRepository)
        }
        return Result.success()
    }

    companion object {
        private const val WorkerName = "update-checker"

        /**
         * Schedule the [UpdateCheckWorker] to be run daily with constraints.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WorkerName,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }

        /**
         * Cancel any scheduled [UpdateCheckWorker].
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WorkerName)
        }
    }
}

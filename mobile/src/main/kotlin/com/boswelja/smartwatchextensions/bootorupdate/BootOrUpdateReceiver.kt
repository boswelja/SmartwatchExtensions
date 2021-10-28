package com.boswelja.smartwatchextensions.bootorupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import timber.log.Timber

/**
 * A [BroadcastReceiver] to handle enqueueing [BootWorker] or [UpdateWorker] as needed.
 */
class BootOrUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("Received a broadcast")

        if (context == null) {
            Timber.w("Null context")
            return
        }

        val workRequest = when (intent?.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                OneTimeWorkRequestBuilder<UpdateWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                OneTimeWorkRequestBuilder<BootWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
            }
            else -> throw IllegalArgumentException(
                "Receiver started with unknown action ${intent?.action}"
            )
        }
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

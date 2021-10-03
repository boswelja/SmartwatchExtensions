package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.app.NotificationManager.INTERRUPTION_FILTER_ALL
import android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY
import android.content.Context
import androidx.core.content.getSystemService
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import timber.log.Timber

/**
 * A [MessageReceiver] that receives DnD changes from the connected phone.
 */
class DnDRemoteChangeReceiver : MessageReceiver<Boolean>(DnDStatusSerializer) {

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Boolean>) {
        val notificationManager = context.getSystemService<NotificationManager>()!!
        notificationManager.setDnD(message.data)
    }

    /**
     * Try to set the system DnD status. This will fail if permission is not granted.
     * @param isEnabled Whether DnD should be enabled.
     * @return true if setting DnD succeeds, false otherwise.
     */
    private fun NotificationManager.setDnD(isEnabled: Boolean): Boolean {
        return if (isNotificationPolicyAccessGranted) {
            val newFilter = if (isEnabled)
                INTERRUPTION_FILTER_PRIORITY
            else
                INTERRUPTION_FILTER_ALL
            setInterruptionFilter(newFilter)
            true
        } else {
            Timber.w("No permission to set DnD state")
            false
        }
    }
}

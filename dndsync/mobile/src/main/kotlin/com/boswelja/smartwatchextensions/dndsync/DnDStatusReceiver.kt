package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.Context
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver

/**
 * A [MessageReceiver] that receives DnD status changes and tries to apply the new value.
 */
class DnDStatusReceiver : MessageReceiver<Boolean>(DnDStatusSerializer) {

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Boolean>) {
        context.getSystemService(NotificationManager::class.java).setDnD(message.data)
    }

    /**
     * Try to set the system DnD status. This will fail if permission is not granted.
     * @param isEnabled Whether DnD should be enabled.
     * @return true if setting DnD succeeds, false otherwise.
     */
    private fun NotificationManager.setDnD(isEnabled: Boolean): Boolean {
        return if (isNotificationPolicyAccessGranted) {
            val newFilter = if (isEnabled)
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            else
                NotificationManager.INTERRUPTION_FILTER_ALL
            setInterruptionFilter(newFilter)
            true
        } else {
            false
        }
    }

}

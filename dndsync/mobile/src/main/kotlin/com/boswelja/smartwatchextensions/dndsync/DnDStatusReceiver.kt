package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.Context
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

/**
 * A [MessageReceiver] that receives DnD status changes and tries to apply the new value.
 */
class DnDStatusReceiver : MessageReceiver() {

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == DnDStatusPath) {
            val dndState = DnDStatusSerializer.deserialize(message.data)
            context.getSystemService(NotificationManager::class.java).setDnD(dndState)
        }
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

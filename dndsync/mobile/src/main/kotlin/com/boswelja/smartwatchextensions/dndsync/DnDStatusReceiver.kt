package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

/**
 * A [WearableListenerService] that receives DnD status changes and tries to apply the new value.
 */
class DnDStatusReceiver : WearableListenerService() {

    override fun onMessageReceived(message: MessageEvent) {
        if (message.path == DnDStatusPath) {
            val dndState = DnDStatusSerializer.deserialize(message.data)
            getSystemService(NotificationManager::class.java).setDnD(dndState)
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

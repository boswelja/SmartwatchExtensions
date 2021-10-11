package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.Context
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

/**
 * A [MessageReceiver] that receives DnD status changes and tries to apply the new value.
 */
abstract class BaseDnDStatusReceiver : MessageReceiver<Boolean>(DnDStatusSerializer) {

    /**
     * Called when the DnD status is successfully updated.
     * @param context [Context].
     * @param sourceUid The UID of the device that triggered the change.
     * @param dndState The new state of DnD.
     */
    open suspend fun onDnDUpdated(context: Context, sourceUid: String, dndState: Boolean) { }

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Boolean>) {
        val success = context.getSystemService(NotificationManager::class.java).setDnD(message.data)
        if (success) onDnDUpdated(context, message.sourceUid, message.data)
    }
}

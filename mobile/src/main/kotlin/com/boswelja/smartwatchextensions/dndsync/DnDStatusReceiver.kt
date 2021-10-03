package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

class DnDStatusReceiver : MessageReceiver<Boolean>(DnDStatusSerializer) {
    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Boolean>) {
        context.getSystemService<NotificationManager>()?.setDnD(message.data)
    }
}

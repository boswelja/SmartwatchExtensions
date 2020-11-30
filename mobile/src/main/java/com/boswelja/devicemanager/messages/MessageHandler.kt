package com.boswelja.devicemanager.messages

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.devicemanager.NotificationChannelHelper
import com.boswelja.devicemanager.messages.database.MessageDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Object to help with posting messages to the system.
 */
object MessageHandler {

    /**
     * Post a message to the system.
     * @param message The [Message] instance to post.
     * @param priority The [Priority] of the message.
     */
    fun postMessage(
        context: Context,
        message: Message,
        priority: Priority = Priority.LOW,
        database: MessageDatabase = MessageDatabase.get(context),
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        coroutineScope.launch {
            val id = database.messageDao().createMessage(message)
            if (priority == Priority.HIGH) {
                val notificationManager: NotificationManager = context.getSystemService()!!
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannelHelper.createForSystemMessages(context, notificationManager)
                }
                val notification =
                    NotificationCompat.Builder(context, MESSAGE_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(message.icon.iconRes)
                        .setContentTitle(message.title)
                        .setContentText(message.text)
                        .build()
                notificationManager.notify(id.toInt(), notification)
            }
        }
    }

    const val MESSAGE_NOTIFICATION_CHANNEL_ID = "system_messages"
}

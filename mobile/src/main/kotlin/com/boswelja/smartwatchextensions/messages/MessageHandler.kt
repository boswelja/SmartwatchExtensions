package com.boswelja.smartwatchextensions.messages

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.messages.database.MessageDatabase
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
        database: MessageDatabase = MessageDatabase.getInstance(context),
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        coroutineScope.launch {
            val id = database.messageDao().createMessage(message)
            if (priority == Priority.HIGH) {
                context.getSystemService<NotificationManager>()?.let {
                    NotificationChannelHelper.createForSystemMessages(context, it)
                    val notification =
                        NotificationCompat.Builder(context, MESSAGE_NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.noti_ic_watch)
                            .setContentTitle(message.title)
                            .setContentText(message.text)
                            .build()
                    it.notify(id.toInt(), notification)
                }
            }
        }
    }

    const val MESSAGE_NOTIFICATION_CHANNEL_ID = "system_messages"
}
package com.boswelja.smartwatchextensions.messages

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.messages.database.MessageDatabase

/**
 * Notification channel ID for our messages
 */
const val MESSAGE_NOTIFICATION_CHANNEL_ID = "system_messages"

/**
 * Puts an active message in the database, and optionally creates a system notification.
 * @param message The [Message] instance to post.
 * @param priority The [Priority] of the message.
 */
suspend fun Context.sendMessage(
    message: Message,
    priority: Priority = Priority.LOW,
    database: MessageDatabase = MessageDatabase.getInstance(this),
    notificationManager: NotificationManager = getSystemService()!!
) {
    val id = database.messageDao().createMessage(message)
    if (priority == Priority.HIGH) {
        NotificationChannelHelper.createForSystemMessages(this, notificationManager)
        val notification =
            NotificationCompat.Builder(this, MESSAGE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.noti_ic_watch)
                .setContentTitle(message.title)
                .setContentText(message.text)
                .build()
        notificationManager.notify(id.toInt(), notification)
    }
}

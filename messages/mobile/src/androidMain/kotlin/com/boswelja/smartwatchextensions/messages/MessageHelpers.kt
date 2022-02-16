package com.boswelja.smartwatchextensions.messages

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

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
    repository: MessagesRepository,
    notificationManager: NotificationManager = getSystemService()!!
) {
    repository.insert(message, null)
    if (priority == Priority.HIGH) {
        createForSystemMessages(this, notificationManager)
        val notification =
            NotificationCompat.Builder(this, MESSAGE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.noti_ic_watch)
                .setContentTitle(message.title)
                .setContentText(message.text)
                .build()
        notificationManager.notify(message.hashCode(), notification)
    }
}

/**
 * Create a notification channel for messages from the system.
 */
private fun createForSystemMessages(context: Context, notificationManager: NotificationManager) {
    NotificationChannel(
        MESSAGE_NOTIFICATION_CHANNEL_ID,
        context.getString(R.string.messages_noti_channel_label),
        NotificationManager.IMPORTANCE_DEFAULT
    ).also {
        notificationManager.createNotificationChannel(it)
    }
}

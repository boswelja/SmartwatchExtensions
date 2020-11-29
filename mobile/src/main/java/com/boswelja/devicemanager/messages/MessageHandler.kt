package com.boswelja.devicemanager.messages

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.DataEvent
import com.boswelja.devicemanager.messages.database.MessageDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Class to handle message-related events. Posting, dismissing etc.
 * This should be favored over directly accessing [MessageDatabase], as it handles priority and
 * other events.
 */
class MessageHandler internal constructor(
    private val context: Context,
    private val database: MessageDatabase = MessageDatabase.get(context),
    private val notificationManager: NotificationManager? = context.getSystemService(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    /* Event that's triggered by dismissing a message. Observe to get the dismissed message ID*/
    val messageDismissedEvent = DataEvent<Long>()

    init {
        if (notificationManager == null) {
            Timber.w("Failed to get NotificationManager instance")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create notification channel if needed
            val notificationChannel = NotificationChannel(
                MESSAGE_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.messages_noti_channel_label),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * Post a message to the system.
     * @param message The [Message] instance to post.
     * @param priority The [Priority] of the message.
     */
    fun postMessage(message: Message, priority: Priority = Priority.LOW) {
        coroutineScope.launch {
            val id = database.messageDao().createMessage(message)
            if (notificationManager != null && priority == Priority.HIGH) {
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

    /**
     * Dismiss a message and notify event listeners.
     * @param messageId The [Message.id] of the message to dismiss.
     */
    fun dismissMessage(messageId: Long) {
        coroutineScope.launch {
            database.messageDao().dismissMessage(messageId)
            messageDismissedEvent.postValue(messageId)
        }
    }

    /**
     * Restore a dismissed message.
     * @param messageId The [Message.id] of the message to restore.
     */
    fun restoreMessage(messageId: Long) {
        coroutineScope.launch {
            database.messageDao().restoreMessage(messageId)
        }
    }

    companion object {
        const val MESSAGE_NOTIFICATION_CHANNEL_ID = "system_messages"

        private var INSTANCE: MessageHandler? = null

        /**
         * Get an instance of [MessageHandler].
         */
        fun get(context: Context): MessageHandler {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = MessageHandler(context)
                }
                return INSTANCE!!
            }
        }
    }
}

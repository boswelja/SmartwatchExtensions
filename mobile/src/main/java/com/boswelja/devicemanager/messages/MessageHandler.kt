package com.boswelja.devicemanager.messages

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.messages.database.MessageDatabase
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MessageHandler internal constructor(
    private val context: Context,
    private val database: MessageDatabase,
    private val notificationManager: NotificationManager?,
    private val coroutineScope: CoroutineScope
) {

    constructor(context: Context) : this(
        context,
        MessageDatabase.get(context),
        context.getSystemService(),
        CoroutineScope(Dispatchers.IO)
    )

    private val notificationId = AtomicInteger(0)

    init {
        if (notificationManager == null) {
            Timber.w("Failed to get NotificationManager instance")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                MESSAGE_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.messages_noti_channel_label),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun postMessage(message: Message, priority: Priority = Priority.LOW) {
        coroutineScope.launch {
            database.messageDao().createMessage(message)
            if (notificationManager != null && priority == Priority.HIGH) {
                val notification =
                    NotificationCompat.Builder(context, MESSAGE_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(message.icon.iconRes)
                        .setContentTitle(message.title)
                        .setContentText(message.text)
                        .build()
                notificationManager.notify(notificationId.incrementAndGet(), notification)
            }
        }
    }

    companion object {
        const val MESSAGE_NOTIFICATION_CHANNEL_ID = "system_messages"
    }
}

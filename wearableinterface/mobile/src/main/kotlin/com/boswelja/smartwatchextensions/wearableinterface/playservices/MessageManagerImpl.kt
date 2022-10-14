package com.boswelja.smartwatchextensions.wearableinterface.playservices

import android.content.Context
import com.boswelja.smartwatchextensions.wearableinterface.MessageManager
import com.boswelja.smartwatchextensions.wearableinterface.MessagePriority
import com.boswelja.smartwatchextensions.wearableinterface.ReceivedMessage
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.MessageOptions
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException

internal class MessageManagerImpl(context: Context) : MessageManager {

    private val messageClient = Wearable.getMessageClient(context)

    override suspend fun sendMessage(watchId: String, path: String, data: ByteArray?, priority: MessagePriority) {
        try {
            messageClient.sendMessage(
                watchId,
                path,
                data,
                MessageOptions(
                    when (priority) {
                        MessagePriority.High -> MessageOptions.MESSAGE_PRIORITY_HIGH
                        MessagePriority.Low -> MessageOptions.MESSAGE_PRIORITY_LOW
                    }
                )
            )
        } catch (e: ApiException) {
            throw IOException(e)
        }
    }

    override fun receiveMessages(): Flow<ReceivedMessage> = callbackFlow {
        val listener = OnMessageReceivedListener {
            trySend(
                ReceivedMessage(
                    it.sourceNodeId,
                    it.path,
                    it.data
                )
            )
        }
        messageClient.addListener(listener)

        awaitClose {
            messageClient.removeListener(listener)
        }
    }
}

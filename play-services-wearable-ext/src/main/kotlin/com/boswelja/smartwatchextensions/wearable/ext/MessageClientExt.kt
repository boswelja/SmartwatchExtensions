package com.boswelja.smartwatchextensions.wearable.ext

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.MessageEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

fun MessageClient.receiveMessages(): Flow<MessageEvent> = callbackFlow {
    val listener = OnMessageReceivedListener {
        trySend(it)
    }
    addListener(listener)

    awaitClose {
        removeListener(listener)
    }
}

suspend fun MessageClient.sendMessage(
    targetId: String,
    path: String,
    data: ByteArray? = null,
    priority: MessagePriority = MessagePriority.Low
): Boolean {
    return try {
        sendMessage(
            targetId,
            path,
            data,
        ).await()
        true
    } catch (_: ApiException) {
        false
    }
}

enum class MessagePriority {
    High,
    Low
}

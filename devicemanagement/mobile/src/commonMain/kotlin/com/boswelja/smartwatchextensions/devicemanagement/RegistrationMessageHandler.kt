package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.core.message.MessageHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * A helper class to provide functions for communicating to a watch when it has been registered.
 * @param messageClient The [MessageClient] to use for requests.
 */
class RegistrationMessageHandler(
    messageClient: MessageClient
) {
    private val messageHandler = MessageHandler(RegistrationSerializer, messageClient)

    /**
     * Notify the device with the target UID that it has been registered.
     * @param targetUid The target device UID.
     * @return See [MessageHandler.sendMessage]
     */
    suspend fun sendRegisteredMessage(targetUid: String) = messageHandler.sendMessage(
        targetUid,
        Message(
            WATCH_REGISTERED_PATH,
            null
        )
    )

    /**
     * Suspends execution until the device with the target UID confirms it's registration. Note
     * there is no timeout by default.
     * @param targetUid The target device UID to listen to a message from.
     * @param timeoutMillis The timeout milliseconds before deciding the message was not received.
     * @return true if the target device acknowledged it's registration, false otherwise.
     */
    suspend fun awaitRegistrationAcknowledged(targetUid: String, timeoutMillis: Long): Boolean {
        val result = withTimeoutOrNull(timeoutMillis) {
            messageHandler.incomingMessages().first { it.sourceUid == targetUid }
        }
        return result != null
    }
}

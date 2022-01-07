package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] to receive [REQUEST_BATTERY_UPDATE_PATH].
 */
class BatteryUpdateRequestReceiver : MessageReceiver(), KoinComponent {

    private val messageClient: MessageClient by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == REQUEST_BATTERY_UPDATE_PATH) {
            val batteryStats = context.batteryStats()!!
            val handler = MessageHandler(BatteryStatsSerializer, messageClient)
            handler.sendMessage(message.sourceUid, Message(BATTERY_STATUS_PATH, batteryStats))
        }
    }
}
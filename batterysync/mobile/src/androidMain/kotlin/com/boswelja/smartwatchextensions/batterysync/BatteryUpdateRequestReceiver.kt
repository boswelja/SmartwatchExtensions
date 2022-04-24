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
 * A [MessageReceiver] to receive [RequestBatteryStatus].
 */
class BatteryUpdateRequestReceiver : MessageReceiver(), KoinComponent {

    private val messageClient: MessageClient by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == RequestBatteryStatus) {
            val batteryStats = context.batteryStats()!!
            val handler = MessageHandler(BatteryStatsSerializer, messageClient)
            handler.sendMessage(message.sourceUid, Message(BatteryStatus, batteryStats))
        }
    }
}

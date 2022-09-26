package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.batterysync.RequestBatteryStatus
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryStats
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.core.message.MessageClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] to receive [RequestBatteryStatus].
 */
class BatteryUpdateRequestReceiver : MessageReceiver(), KoinComponent {

    private val messageClient: MessageClient by inject()
    private val getPhoneBatteryStats: GetPhoneBatteryStats by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == RequestBatteryStatus) {
            val batteryStats = getPhoneBatteryStats().getOrNull()
            if (batteryStats != null) {
                messageClient.sendMessage(
                    message.sourceUid,
                    Message(
                        BatteryStatus,
                        BatteryStatsSerializer.serialize(batteryStats)
                    )
                )
            }
        }
    }
}

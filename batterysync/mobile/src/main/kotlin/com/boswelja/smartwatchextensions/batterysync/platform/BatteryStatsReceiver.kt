package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.StoreBatteryStatsForWatch
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] to receive [BatteryStats] and store it.
 */
class BatteryStatsReceiver :
    MessageReceiver(),
    KoinComponent {

    private val storeBatteryStatsForWatch: StoreBatteryStatsForWatch by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == BatteryStatus) {
            val batteryStats = message.data?.let { BatteryStatsSerializer.deserialize(it) } ?: return
            storeBatteryStatsForWatch(message.sourceUid, batteryStats)
        }
    }
}

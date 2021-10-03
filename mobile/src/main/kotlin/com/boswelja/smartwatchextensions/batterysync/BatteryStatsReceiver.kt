package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.Utils.handleBatteryStats
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

class BatteryStatsReceiver : MessageReceiver<BatteryStats?>(BatteryStatsSerializer) {
    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats?>
    ) {
        message.data?.let { batteryStats ->
            handleBatteryStats(context, message.sourceUid, batteryStats)
        }
    }
}

package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient

class SendUpdatedBatteryStatsToWatch(
    private val messageClient: MessageClient
) {
    suspend operator fun invoke(watchId: String, batteryStats: BatteryStats): Boolean {
        return messageClient.sendMessage(
            watchId,
            Message(
                BatteryStatus,
                BatteryStatsSerializer.serialize(batteryStats)
            )
        )
    }
}

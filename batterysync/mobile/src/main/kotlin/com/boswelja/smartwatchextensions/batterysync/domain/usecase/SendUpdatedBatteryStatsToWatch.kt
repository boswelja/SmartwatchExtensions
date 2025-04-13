package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.smartwatchextensions.wearable.ext.sendMessage
import com.google.android.gms.wearable.MessageClient

class SendUpdatedBatteryStatsToWatch(
    private val messageClient: MessageClient
) {
    suspend operator fun invoke(watchId: String, batteryStats: BatteryStats): Boolean {
        return messageClient.sendMessage(
            targetId = watchId,
            path = BatteryStatus,
            data = BatteryStatsSerializer.serialize(batteryStats)
        )
    }
}

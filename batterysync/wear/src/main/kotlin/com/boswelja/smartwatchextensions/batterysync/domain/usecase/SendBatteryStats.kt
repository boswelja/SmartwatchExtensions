package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatus
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient

class SendBatteryStats(
    private val messageClient: MessageClient,
    private val discoveryClient: DiscoveryClient
) {
    suspend operator fun invoke(batteryStats: BatteryStats): Boolean {
        val phoneId = discoveryClient.pairedPhone()?.uid ?: return false

        return messageClient.sendMessage(
            phoneId,
            Message(
                BatteryStatus,
                BatteryStatsSerializer.serialize(batteryStats)
            )
        )
    }
}

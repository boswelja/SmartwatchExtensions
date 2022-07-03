package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import com.boswelja.smartwatchextensions.batterysync.RequestBatteryStatus
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient

class RequestBatteryStatsUpdate(
    private val messageClient: MessageClient,
    private val discoveryClient: DiscoveryClient
) {
    suspend operator fun invoke(): Boolean {
        val phoneId = discoveryClient.pairedPhone()!!.uid
        return messageClient.sendMessage(
            phoneId,
            Message(
                RequestBatteryStatus,
                null
            )
        )
    }
}

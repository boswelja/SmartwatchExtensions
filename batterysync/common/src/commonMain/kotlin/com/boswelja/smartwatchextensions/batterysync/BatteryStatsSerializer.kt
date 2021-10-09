package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.watchconnection.common.message.MessageSerializer

object BatteryStatsSerializer : MessageSerializer<BatteryStats?>(
    messagePaths = setOf(BATTERY_STATUS_PATH)
) {
    override suspend fun deserialize(bytes: ByteArray): BatteryStats? {
        return try {
            BatteryStats.ADAPTER.decode(bytes)
        } catch (e: Exception) {
            null
        }
    }
    override suspend fun serialize(data: BatteryStats?): ByteArray = data!!.encode()
}

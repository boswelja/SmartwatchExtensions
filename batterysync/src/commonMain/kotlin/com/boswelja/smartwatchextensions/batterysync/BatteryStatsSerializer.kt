package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.watchconnection.common.message.MessageSerializer

class BatteryStatsSerializer(
    private val onDeserializeException: (suspend (e: Exception) -> Unit)? = null
) : MessageSerializer<BatteryStats?>(
    messagePaths = setOf(BATTERY_STATUS_PATH)
) {
    override suspend fun deserialize(bytes: ByteArray): BatteryStats? {
        return try {
            BatteryStats.ADAPTER.decode(bytes)
        } catch (e: Exception) {
            onDeserializeException?.invoke(e)
            null
        }
    }
    override suspend fun serialize(data: BatteryStats?): ByteArray = data!!.encode()
}

package com.boswelja.smartwatchextensions.common.batterysync

import com.boswelja.smartwatchextensions.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.watchconnection.common.message.serialized.MessageSerializer

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

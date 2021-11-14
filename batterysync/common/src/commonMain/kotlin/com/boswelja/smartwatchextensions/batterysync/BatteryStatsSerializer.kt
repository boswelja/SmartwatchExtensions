package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.watchconnection.serialization.MessageSerializer
import okio.IOException

/**
 * A [MessageSerializer] for handling [BatteryStats].
 */
object BatteryStatsSerializer : MessageSerializer<BatteryStats?> {
    override val messagePaths: Set<String> = setOf(BATTERY_STATUS_PATH)

    override suspend fun deserialize(bytes: ByteArray?): BatteryStats? {
        return try {
            BatteryStats.ADAPTER.decode(bytes!!)
        } catch (_: IOException) {
            null
        }
    }
    override suspend fun serialize(data: BatteryStats?): ByteArray = data!!.encode()
}

package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] for handling [BatteryStats].
 */
object BatteryStatsSerializer : MessageSerializer<BatteryStats?> {
    override val messagePaths: Set<String> = setOf(BATTERY_STATUS_PATH)

    override suspend fun deserialize(bytes: ByteArray?): BatteryStats? {
        return try {
            bytes?.let { ProtoBuf.decodeFromByteArray(it) }
        } catch (ignored: SerializationException) {
            null
        }
    }

    override suspend fun serialize(data: BatteryStats?): ByteArray = ProtoBuf.encodeToByteArray(data)
}

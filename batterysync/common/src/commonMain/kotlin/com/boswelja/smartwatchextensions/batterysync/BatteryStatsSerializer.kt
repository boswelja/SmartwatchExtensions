package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] for handling [BatteryStats].
 */
@OptIn(ExperimentalSerializationApi::class)
object BatteryStatsSerializer : MessageSerializer<BatteryStats> {
    override val messagePaths: Set<String> = setOf(BATTERY_STATUS_PATH)

    override suspend fun deserialize(bytes: ByteArray?): BatteryStats {
        try {
            return ProtoBuf.decodeFromByteArray(bytes!!)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to deserialize battery stats!", e)
        }
    }

    override suspend fun serialize(data: BatteryStats): ByteArray = ProtoBuf.encodeToByteArray(data)
}

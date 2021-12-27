package com.boswelja.smartwatchextensions.settings

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] for handling [BoolSetting].
 */
@OptIn(ExperimentalSerializationApi::class)
object BoolSettingSerializer : MessageSerializer<BoolSetting> {

    override val messagePaths: Set<String> = setOf(UPDATE_BOOL_PREFERENCE)

    override suspend fun deserialize(
        bytes: ByteArray?
    ): BoolSetting = ProtoBuf.decodeFromByteArray(bytes!!)

    override suspend fun serialize(data: BoolSetting): ByteArray = ProtoBuf.encodeToByteArray(data)
}

package com.boswelja.smartwatchextensions.settings

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] to handle [IntSetting].
 */
@OptIn(ExperimentalSerializationApi::class)
object IntSettingSerializer : MessageSerializer<IntSetting> {
    override val messagePaths: Set<String> = setOf(UPDATE_INT_PREFERENCE)

    override suspend fun deserialize(
        bytes: ByteArray?
    ): IntSetting = ProtoBuf.decodeFromByteArray(bytes!!)

    override suspend fun serialize(data: IntSetting): ByteArray = ProtoBuf.encodeToByteArray(data)
}

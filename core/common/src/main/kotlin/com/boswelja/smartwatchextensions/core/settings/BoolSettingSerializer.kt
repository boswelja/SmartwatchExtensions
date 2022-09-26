package com.boswelja.smartwatchextensions.core.settings

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A serializer for handling [BoolSetting].
 */
@OptIn(ExperimentalSerializationApi::class)
object BoolSettingSerializer {

    fun deserialize(
        bytes: ByteArray
    ): BoolSetting = ProtoBuf.decodeFromByteArray(bytes)

    fun serialize(data: BoolSetting): ByteArray = ProtoBuf.encodeToByteArray(data)
}

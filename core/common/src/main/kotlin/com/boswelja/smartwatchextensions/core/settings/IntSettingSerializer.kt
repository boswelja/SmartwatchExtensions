package com.boswelja.smartwatchextensions.core.settings

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A serializer for handling [IntSetting].
 */
@OptIn(ExperimentalSerializationApi::class)
object IntSettingSerializer {

    fun deserialize(
        bytes: ByteArray
    ): IntSetting = ProtoBuf.decodeFromByteArray(bytes)

    fun serialize(data: IntSetting): ByteArray = ProtoBuf.encodeToByteArray(data)
}

package com.boswelja.smartwatchextensions.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A serializer for handling [Version].
 */
@OptIn(ExperimentalSerializationApi::class)
object VersionSerializer {
    fun deserialize(bytes: ByteArray): Version = ProtoBuf.decodeFromByteArray(bytes)
    fun serialize(data: Version): ByteArray = ProtoBuf.encodeToByteArray(data)
}

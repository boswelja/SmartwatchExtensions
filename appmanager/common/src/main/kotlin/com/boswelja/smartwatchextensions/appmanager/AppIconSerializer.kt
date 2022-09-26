package com.boswelja.smartwatchextensions.appmanager

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A serializer for handling serialization of [AppIcon]
 */
@OptIn(ExperimentalSerializationApi::class)
object AppIconSerializer {
    fun deserialize(bytes: ByteArray?): AppIcon = ProtoBuf.decodeFromByteArray(bytes!!)
    fun serialize(data: AppIcon): ByteArray = ProtoBuf.encodeToByteArray(data)
}

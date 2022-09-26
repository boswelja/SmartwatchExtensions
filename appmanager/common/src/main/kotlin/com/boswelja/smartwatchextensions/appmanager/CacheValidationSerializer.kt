package com.boswelja.smartwatchextensions.appmanager

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A serializer for handling serialization for a cache hash.
 */
@OptIn(ExperimentalSerializationApi::class)
object CacheValidationSerializer {

    fun deserialize(bytes: ByteArray): AppVersions = ProtoBuf.decodeFromByteArray(bytes)

    fun serialize(data: AppVersions): ByteArray = ProtoBuf.encodeToByteArray(data)
}

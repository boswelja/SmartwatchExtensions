package com.boswelja.smartwatchextensions.appmanager

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A serializer for handling [RemovedApps]
 */
@OptIn(ExperimentalSerializationApi::class)
object RemovedAppsSerializer {

    fun deserialize(bytes: ByteArray?): RemovedApps = ProtoBuf.decodeFromByteArray(bytes!!)

    fun serialize(data: RemovedApps): ByteArray = ProtoBuf.encodeToByteArray(data)
}

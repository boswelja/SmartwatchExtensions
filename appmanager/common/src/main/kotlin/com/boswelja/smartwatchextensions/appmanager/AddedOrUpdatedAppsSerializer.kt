package com.boswelja.smartwatchextensions.appmanager

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A serializer for handling serialization for [AppList].
 */
@OptIn(ExperimentalSerializationApi::class)
object AddedOrUpdatedAppsSerializer {

    fun deserialize(bytes: ByteArray): AppList = ProtoBuf.decodeFromByteArray(bytes)
    fun serialize(data: AppList): ByteArray = ProtoBuf.encodeToByteArray(data)
}

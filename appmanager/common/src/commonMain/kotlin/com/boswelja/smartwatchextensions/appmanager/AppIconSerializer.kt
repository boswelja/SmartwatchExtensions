package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] to handle serialization of [AppIcon]
 */
@OptIn(ExperimentalSerializationApi::class)
object AppIconSerializer : MessageSerializer<AppIcon> {
    override val messagePaths: Set<String> = setOf(APP_ICON)
    override suspend fun deserialize(bytes: ByteArray?): AppIcon = ProtoBuf.decodeFromByteArray(bytes!!)
    override suspend fun serialize(data: AppIcon): ByteArray = ProtoBuf.encodeToByteArray(data)
}

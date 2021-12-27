package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] for handling [Version].
 */
@OptIn(ExperimentalSerializationApi::class)
object VersionSerializer : MessageSerializer<Version> {
    override val messagePaths: Set<String> = setOf(REQUEST_APP_VERSION)
    override suspend fun deserialize(bytes: ByteArray?): Version = ProtoBuf.decodeFromByteArray(bytes!!)
    override suspend fun serialize(data: Version): ByteArray = ProtoBuf.encodeToByteArray(data)
}

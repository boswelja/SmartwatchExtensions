package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] for handling [RemovedApps]
 */
@OptIn(ExperimentalSerializationApi::class)
object RemovedAppsSerializer : MessageSerializer<RemovedApps> {
    override val messagePaths: Set<String> = setOf(REMOVED_APPS)

    override suspend fun deserialize(bytes: ByteArray?): RemovedApps =
        ProtoBuf.decodeFromByteArray(bytes!!)

    override suspend fun serialize(data: RemovedApps): ByteArray = ProtoBuf.encodeToByteArray(data)
}

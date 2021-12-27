package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] to handle serialization for [AppList].
 */
@OptIn(ExperimentalSerializationApi::class)
object AddedOrUpdatedAppsSerializer : MessageSerializer<AppList> {
    override val messagePaths: Set<String> = setOf(
        ADDED_APPS,
        UPDATED_APPS
    )

    override suspend fun deserialize(bytes: ByteArray?): AppList = ProtoBuf.decodeFromByteArray(bytes!!)
    override suspend fun serialize(data: AppList): ByteArray = ProtoBuf.encodeToByteArray(data)
}

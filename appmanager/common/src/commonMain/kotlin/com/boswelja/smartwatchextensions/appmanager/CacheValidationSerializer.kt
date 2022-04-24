package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * A [MessageSerializer] to handle serialization for a cache hash.
 */
@OptIn(ExperimentalSerializationApi::class)
object CacheValidationSerializer : MessageSerializer<AppVersions> {

    override val messagePaths: Set<String> = setOf(RequestValidateCache)

    override suspend fun deserialize(bytes: ByteArray?): AppVersions =
        ProtoBuf.decodeFromByteArray(bytes!!)

    override suspend fun serialize(data: AppVersions): ByteArray = ProtoBuf.encodeToByteArray(data)
}

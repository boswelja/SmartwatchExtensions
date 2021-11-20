package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer

/**
 * A [MessageSerializer] to handle serialization for a cache hash.
 */
object CacheValidationSerializer : MessageSerializer<AppVersions> {

    override val messagePaths: Set<String> = setOf(VALIDATE_CACHE)

    override suspend fun deserialize(bytes: ByteArray?): AppVersions =
        AppVersions.ADAPTER.decode(bytes!!)

    override suspend fun serialize(data: AppVersions): ByteArray = data.encode()
}

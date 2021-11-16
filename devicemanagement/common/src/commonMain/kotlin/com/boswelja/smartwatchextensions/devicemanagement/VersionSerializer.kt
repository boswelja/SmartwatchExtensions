package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.watchconnection.serialization.MessageSerializer

/**
 * A [MessageSerializer] for handling [Version].
 */
object VersionSerializer : MessageSerializer<Version> {
    override val messagePaths: Set<String> = setOf(REQUEST_APP_VERSION)
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun deserialize(bytes: ByteArray?): Version = Version.ADAPTER.decode(bytes!!)
    override suspend fun serialize(data: Version): ByteArray = Version.ADAPTER.encode(data)
}

package com.boswelja.smartwatchextensions.common.versioning

import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_APP_VERSION
import com.boswelja.watchconnection.common.message.MessageSerializer

object VersionSerializer : MessageSerializer<Version>(
    messagePaths = setOf(REQUEST_APP_VERSION)
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun deserialize(bytes: ByteArray): Version = Version.ADAPTER.decode(bytes)
    override suspend fun serialize(data: Version): ByteArray = Version.ADAPTER.encode(data)
}

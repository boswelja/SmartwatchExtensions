package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer

object RemovedAppsSerializer : MessageSerializer<RemovedApps> {
    override val messagePaths: Set<String> = setOf(REMOVED_APPS)

    override suspend fun deserialize(bytes: ByteArray?): RemovedApps =
        RemovedApps.ADAPTER.decode(bytes!!)

    override suspend fun serialize(data: RemovedApps): ByteArray = data.encode()
}

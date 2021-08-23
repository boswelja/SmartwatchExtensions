package com.boswelja.smartwatchextensions.common.appmanager

import com.boswelja.watchconnection.core.message.serialized.MessageSerializer

object AppListSerializer : MessageSerializer<AppList>(
    messagePaths = setOf(
        Messages.APP_DATA
    )
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun deserialize(bytes: ByteArray): AppList = AppList.ADAPTER.decode(bytes)
    override suspend fun serialize(data: AppList): ByteArray = AppList.ADAPTER.encode(data)
}

package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.common.message.MessageSerializer

/**
 * A [MessageSerializer] to handle serialization for [AppList].
 */
object AppListSerializer : MessageSerializer<AppList>(
    messagePaths = setOf(
        APP_LIST
    )
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun deserialize(bytes: ByteArray): AppList = AppList.ADAPTER.decode(bytes)
    override suspend fun serialize(data: AppList): ByteArray = data.encode()
}

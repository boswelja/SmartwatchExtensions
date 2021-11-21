package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer

/**
 * A [MessageSerializer] to handle serialization for [AppList].
 */
object AddedOrUpdatedAppsSerializer : MessageSerializer<AppList> {
    override val messagePaths: Set<String> = setOf(
        ADDED_APPS,
        UPDATED_APPS
    )

    override suspend fun deserialize(bytes: ByteArray?): AppList = AppList.ADAPTER.decode(bytes!!)
    override suspend fun serialize(data: AppList): ByteArray = data.encode()
}
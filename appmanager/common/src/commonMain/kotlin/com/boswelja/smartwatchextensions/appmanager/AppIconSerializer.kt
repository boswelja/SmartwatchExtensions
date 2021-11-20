package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer

/**
 * A [MessageSerializer] to handle serialization of [AppIcon]
 */
object AppIconSerializer : MessageSerializer<AppIcon> {
    override val messagePaths: Set<String> = setOf(APP_ICON)
    override suspend fun deserialize(bytes: ByteArray?): AppIcon = AppIcon.ADAPTER.decode(bytes!!)
    override suspend fun serialize(data: AppIcon): ByteArray = data.encode()
}

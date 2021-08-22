@file:Suppress("BlockingMethodInNonBlockingContext")

package com.boswelja.smartwatchextensions.common.appmanager

import com.boswelja.smartwatchextensions.common.appmanager.Messages.APP_DATA
import com.boswelja.watchconnection.core.message.serialized.MessageSerializer

object AppSerializer : MessageSerializer<App>(
    messagePaths = setOf(
        APP_DATA
    )
) {
    override suspend fun deserialize(bytes: ByteArray): App = App.ADAPTER.decode(bytes)
    override suspend fun serialize(data: App): ByteArray = App.ADAPTER.encode(data)
}

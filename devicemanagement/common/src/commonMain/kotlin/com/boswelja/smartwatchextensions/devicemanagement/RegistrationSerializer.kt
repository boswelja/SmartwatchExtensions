package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.watchconnection.common.message.MessageSerializer

/**
 * A [MessageSerializer] for handling registration messages.
 */
object RegistrationSerializer : MessageSerializer<Unit?>(
    setOf(
        WATCH_REGISTERED_PATH
    )
) {
    override suspend fun deserialize(bytes: ByteArray): Unit? { return null }
    override suspend fun serialize(data: Unit?): ByteArray = byteArrayOf()
}

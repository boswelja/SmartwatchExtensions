package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.common.message.MessageSerializer

object PackageNameSerializer : MessageSerializer<String>(
    messagePaths = setOf(
        REQUEST_OPEN_PACKAGE,
        REQUEST_UNINSTALL_PACKAGE
    )
) {
    override suspend fun deserialize(bytes: ByteArray): String = bytes.decodeToString()
    override suspend fun serialize(data: String): ByteArray = data.encodeToByteArray()
}

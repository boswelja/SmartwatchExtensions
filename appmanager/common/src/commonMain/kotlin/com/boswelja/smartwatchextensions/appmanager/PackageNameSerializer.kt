package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.common.message.MessageSerializer

object PackageNameSerializer : MessageSerializer<String>(
    messagePaths = setOf(
        REQUEST_OPEN_PACKAGE,
        REQUEST_UNINSTALL_PACKAGE
    )
) {
    override suspend fun deserialize(bytes: ByteArray): String {
        val decodedString = bytes.decodeToString()
        check(decodedString.length > 1 && decodedString.contains('.')) {
            "$decodedString not a valid package name"
        }
        return decodedString
    }
    override suspend fun serialize(data: String): ByteArray = data.encodeToByteArray()
}

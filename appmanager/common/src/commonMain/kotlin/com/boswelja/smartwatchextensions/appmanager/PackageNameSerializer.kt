package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer

/**
 * A [MessageSerializer] to handle serialization for a package name.
 */
object PackageNameSerializer : MessageSerializer<String> {
    override val messagePaths: Set<String> = setOf(
        RequestOpenPackage,
        RequestUninstallPackage
    )

    override suspend fun deserialize(bytes: ByteArray?): String {
        val decodedString = bytes!!.decodeToString()
        check(decodedString.length > 1 && decodedString.contains('.')) {
            "$decodedString not a valid package name"
        }
        return decodedString
    }
    override suspend fun serialize(data: String): ByteArray = data.encodeToByteArray()
}

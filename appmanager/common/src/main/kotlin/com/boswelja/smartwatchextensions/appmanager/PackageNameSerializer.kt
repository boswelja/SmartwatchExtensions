package com.boswelja.smartwatchextensions.appmanager

/**
 * A serializer for handling serialization for a package name.
 */
object PackageNameSerializer {
    fun deserialize(bytes: ByteArray): String {
        val decodedString = bytes.decodeToString()
        check(decodedString.length > 1 && decodedString.contains('.')) {
            "$decodedString not a valid package name"
        }
        return decodedString
    }
    fun serialize(data: String): ByteArray = data.encodeToByteArray()
}

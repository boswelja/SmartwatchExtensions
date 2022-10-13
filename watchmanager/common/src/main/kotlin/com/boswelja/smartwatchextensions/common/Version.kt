package com.boswelja.smartwatchextensions.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Contains information about the installed version of Smartwatch Extensions on a device.
 * @param versionCode The installed version code.
 * @param versionName The installed version name.
 */
@Serializable
data class Version(
    val versionCode: Long,
    val versionName: String
)

/**
 * Request the target device app version.
 */
const val RequestAppVersion = "/request_app_version"

/**
 * A serializer for handling [Version].
 */
@OptIn(ExperimentalSerializationApi::class)
object VersionSerializer {
    fun deserialize(bytes: ByteArray): Version = ProtoBuf.decodeFromByteArray(bytes)
    fun serialize(data: Version): ByteArray = ProtoBuf.encodeToByteArray(data)
}

package com.boswelja.smartwatchextensions.extensions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

/**
 * Contains settings needed to operate extensions on the watch.
 * @param phoneLockingEnabled Whether phone locking is enabled.
 * @param batterySyncEnabled Whether battery sync is enabled.
 * @param phoneChargeNotiEnabled Whether phone charge notifications are enabled.
 * @param phoneLowNotiEnabled Whether phone low notifications are enabled.
 * @param batteryLowThreshold The battery percent threshold to consider the battery "low".
 * @param batteryChargeThreshold The battery percent threshold to consider the battery "charged".
 * @param dndSyncToPhone Whether DnD Sync to Phone is enabled.
 * @param dndSyncWithTheater Whether DnD Sync with Theater is enabled.
 * @param phoneSeparationNotis Whether phone separation alerts are enabled.
 */
@Serializable
data class ExtensionSettings(
    val phoneLockingEnabled: Boolean,
    val batterySyncEnabled: Boolean,
    val phoneChargeNotiEnabled: Boolean,
    val phoneLowNotiEnabled: Boolean,
    val batteryLowThreshold: Int,
    val batteryChargeThreshold: Int,
    val dndSyncToPhone: Boolean,
    val dndSyncWithTheater: Boolean,
    val phoneSeparationNotis: Boolean
)

/**
 * A [DataStore] for tracking [ExtensionSettings].
 */
val Context.extensionSettingsStore: DataStore<ExtensionSettings> by dataStore(
    "extensionSettings.pb",
    SettingsSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private class SettingsSerializer : Serializer<ExtensionSettings> {
    override val defaultValue = ExtensionSettings(
        phoneLockingEnabled = false,
        batterySyncEnabled = false,
        phoneChargeNotiEnabled = false,
        phoneLowNotiEnabled = false,
        batteryLowThreshold = 15,
        batteryChargeThreshold = 90,
        dndSyncToPhone = false,
        dndSyncWithTheater = false,
        phoneSeparationNotis = false
    )

    override suspend fun readFrom(input: InputStream): ExtensionSettings {
        return ProtoBuf.decodeFromByteArray(input.readBytes())
    }

    override suspend fun writeTo(t: ExtensionSettings, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(t))
    }
}

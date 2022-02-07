package com.boswelja.smartwatchextensions.extensions

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
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
 * @param phoneSeparationNotis Whether phone separation alerts are enabled.
 */
@Serializable
data class ExtensionSettings(
    val phoneSeparationNotis: Boolean
)

/**
 * A [DataStore] for tracking [ExtensionSettings].
 */
val Context.extensionSettingsStore: DataStore<ExtensionSettings> by dataStore(
    "extensionSettings.pb",
    SettingsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { SettingsSerializer.defaultValue }
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private object SettingsSerializer : Serializer<ExtensionSettings> {
    override val defaultValue = ExtensionSettings(
        phoneSeparationNotis = false
    )

    override suspend fun readFrom(input: InputStream): ExtensionSettings {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (_: Exception) {
            throw CorruptionException("ExtensionsSettings corrupted")
        }
    }

    override suspend fun writeTo(t: ExtensionSettings, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(t))
    }
}

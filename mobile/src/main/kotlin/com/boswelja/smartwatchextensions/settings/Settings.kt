package com.boswelja.smartwatchextensions.settings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

/**
 * Contains global app settings.
 * @param analyticsEnabled Whether analytics are enabled.
 * @param qsTileWatchId The watch UID to use for QS Tiles.
 * @param checkForUpdates Whether the app should automatically check for updates.
 */
@Serializable
data class Settings(
    val analyticsEnabled: Boolean,
    val qsTileWatchId: String,
    val checkForUpdates: Boolean
)

/**
 * A [DataStore] for tracking [Settings].
 */
val Context.appSettingsStore: DataStore<Settings> by dataStore(
    "appSettings.pb",
    AppSettingsSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private class AppSettingsSerializer : Serializer<Settings> {
    override val defaultValue = Settings(
        true,
        "",
        false
    )

    override suspend fun readFrom(input: InputStream): Settings {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (exception: SerializationException) {
            throw CorruptionException("Cannot read proto", exception)
        }
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(t))
    }
}

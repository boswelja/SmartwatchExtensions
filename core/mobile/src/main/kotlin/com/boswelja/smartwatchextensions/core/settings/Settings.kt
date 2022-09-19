package com.boswelja.smartwatchextensions.core.settings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
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
 * @param analyticsEnabled Whether analytics are enabled. This is currently unused, but held for future compatibility
 * @param qsTileWatchId The watch UID to use for QS Tiles.
 */
@Serializable
data class Settings(
    val analyticsEnabled: Boolean,
    val qsTileWatchId: String,
)

/**
 * A [DataStore] for tracking [Settings].
 */
val Context.appSettingsStore: DataStore<Settings> by dataStore(
    fileName = "appSettings.pb",
    serializer = AppSettingsSerializer(),
    corruptionHandler = ReplaceFileCorruptionHandler {
        Settings(
            false,
            ""
        )
    }
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private class AppSettingsSerializer : Serializer<Settings> {
    override val defaultValue = Settings(
        false,
        ""
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

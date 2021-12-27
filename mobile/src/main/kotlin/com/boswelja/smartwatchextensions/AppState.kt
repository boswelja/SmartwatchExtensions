package com.boswelja.smartwatchextensions

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Contains information about the global app state.
 * @param hasDonated Whether the user has donated.
 * @param lastAppVersion The version of the app that was last installed.
 */
@Serializable
data class AppState(
    val hasDonated: Boolean,
    val lastAppVersion: Int
)

/**
 * A [DataStore] for tracking [AppState].
 */
val Context.appStateStore: DataStore<AppState> by dataStore(
    "appState.pb",
    AppStateSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private class AppStateSerializer : Serializer<AppState> {
    override val defaultValue = AppState(
        false,
        0
    )

    override suspend fun readFrom(input: InputStream): AppState {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto", exception)
        }
    }

    override suspend fun writeTo(t: AppState, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(t))
    }
}

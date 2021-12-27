package com.boswelja.smartwatchextensions.devicemanagement

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

/**
 * A [DataStore] for storing [SelectedWatchState].
 */
val Context.selectedWatchStateStore: DataStore<SelectedWatchState> by dataStore(
    "selectedWatchState.pb",
    SelectedWatchStateSerializer()
)

@OptIn(ExperimentalSerializationApi::class)
private class SelectedWatchStateSerializer : Serializer<SelectedWatchState> {
    override val defaultValue = SelectedWatchState(
        ""
    )

    override suspend fun readFrom(input: InputStream): SelectedWatchState {
        return try {
            ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (_: SerializationException) {
            defaultValue
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: SelectedWatchState, output: OutputStream) =
        output.write(ProtoBuf.encodeToByteArray(t))
}

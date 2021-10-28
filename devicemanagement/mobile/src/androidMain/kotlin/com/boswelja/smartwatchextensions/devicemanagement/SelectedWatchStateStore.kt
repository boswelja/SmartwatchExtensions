package com.boswelja.smartwatchextensions.devicemanagement

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A [DataStore] for storing [SelectedWatchState].
 */
val Context.selectedWatchStateStore: DataStore<SelectedWatchState> by dataStore(
    "selectedWatchState.pb",
    SelectedWatchStateSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
private class SelectedWatchStateSerializer : Serializer<SelectedWatchState> {
    override val defaultValue = SelectedWatchState(
        ""
    )

    override suspend fun readFrom(input: InputStream): SelectedWatchState {
        return try {
            SelectedWatchState.ADAPTER.decode(input)
        } catch (_: IOException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SelectedWatchState, output: OutputStream) {
        t.encode(output)
    }
}

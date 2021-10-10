package com.boswelja.smartwatchextensions.watchmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

val Context.selectedWatchStateStore: DataStore<SelectedWatchState> by dataStore(
    "selectedWatchState.pb",
    SelectedWatchStateSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
class SelectedWatchStateSerializer : Serializer<SelectedWatchState> {
    override val defaultValue = SelectedWatchState(
        ""
    )

    override suspend fun readFrom(input: InputStream): SelectedWatchState {
        return try {
            SelectedWatchState.ADAPTER.decode(input)
        } catch (exception: IOException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SelectedWatchState, output: OutputStream) {
        t.encode(output)
    }
}

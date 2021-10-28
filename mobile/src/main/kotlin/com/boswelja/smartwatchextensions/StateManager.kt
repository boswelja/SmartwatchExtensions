package com.boswelja.smartwatchextensions

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A [DataStore] for tracking [AppState].
 */
val Context.appStateStore: DataStore<AppState> by dataStore(
    "appState.pb",
    AppStateSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
private class AppStateSerializer : Serializer<AppState> {
    override val defaultValue = AppState(
        false,
        0
    )

    override suspend fun readFrom(input: InputStream): AppState {
        try {
            return AppState.ADAPTER.decode(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto", exception)
        }
    }

    override suspend fun writeTo(t: AppState, output: OutputStream) {
        AppState.ADAPTER.encode(output, t)
    }
}

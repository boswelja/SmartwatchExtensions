package com.boswelja.devicemanager

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

val Context.appStateStore: DataStore<AppState> by dataStore(
    "appState.pb",
    AppStateSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
class AppStateSerializer : Serializer<AppState> {
    override val defaultValue = AppState(
        false,
        ""
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

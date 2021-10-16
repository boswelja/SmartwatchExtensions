package com.boswelja.smartwatchextensions

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

val Context.appStateStore: DataStore<AppState> by dataStore(
    "appState.pb",
    AppStateSerializer(),
    produceMigrations = {
        migrations
    }
)

@Suppress("BlockingMethodInNonBlockingContext")
class AppStateSerializer : Serializer<AppState> {
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

private val migrations = listOf(
    // TODO Remove this migration after initial release, we only need to reset lastAppVersion once
    object : DataMigration<AppState> {
        override suspend fun shouldMigrate(currentData: AppState): Boolean {
            return currentData.lastAppVersion > 100
        }

        override suspend fun migrate(currentData: AppState): AppState {
            return currentData.copy(lastAppVersion = 1)
        }

        override suspend fun cleanUp() { }
    }
)

package com.boswelja.smartwatchextensions.settings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A [DataStore] for tracking [Settings].
 */
val Context.appSettingsStore: DataStore<Settings> by dataStore(
    "appSettings.pb",
    AppSettingsSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
private class AppSettingsSerializer : Serializer<Settings> {
    override val defaultValue = Settings(
        true,
        "",
        false
    )

    override suspend fun readFrom(input: InputStream): Settings {
        try {
            return Settings.ADAPTER.decode(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto", exception)
        }
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        Settings.ADAPTER.encode(output, t)
    }
}

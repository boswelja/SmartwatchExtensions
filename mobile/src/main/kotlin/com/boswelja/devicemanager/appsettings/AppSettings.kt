package com.boswelja.devicemanager.appsettings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

val Context.appSettingsStore: DataStore<Settings> by dataStore(
    "app_settings.pb",
    AppSettingsSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
class AppSettingsSerializer : Serializer<Settings> {
    override val defaultValue = Settings(
        true,
        Settings.Theme.FOLLOW_SYSTEM,
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

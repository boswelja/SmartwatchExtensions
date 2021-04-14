package com.boswelja.smartwatchextensions

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.boswelja.smartwatchextensions.appsettings.Settings
import com.boswelja.smartwatchextensions.appsettings.appSettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // TODO We shouldn't have a blocking call on the main thread like this
        val storedNightMode = appSettingsStore.data.map { it.appTheme }
        val nightMode = when (runBlocking { storedNightMode.first() }) {
            Settings.Theme.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            Settings.Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Settings.Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}

package com.boswelja.devicemanager

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.boswelja.devicemanager.appsettings.Settings
import com.boswelja.devicemanager.appsettings.appSettingsStore
import com.boswelja.devicemanager.bootorupdate.BootOrUpdateHandlerService
import com.boswelja.devicemanager.bootorupdate.updater.Updater
import kotlinx.coroutines.flow.first
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
        val nightMode = when (runBlocking { appSettingsStore.data.first().appTheme }) {
            Settings.Theme.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            Settings.Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Settings.Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        ensureEnvironmentUpdated()
    }

    /** Checks the environment is up to date, otherwise starts a [BootOrUpdateHandlerService]. */
    private fun ensureEnvironmentUpdated() {
        val updater = Updater(this)
        if (updater.needsUpdate) {
            Timber.i("Starting updater service")
            Intent(this, BootOrUpdateHandlerService::class.java)
                .apply { action = Intent.ACTION_MY_PACKAGE_REPLACED }
                .also { ContextCompat.startForegroundService(this, it) }
        }
    }
}

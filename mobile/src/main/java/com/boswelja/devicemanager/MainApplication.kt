package com.boswelja.devicemanager

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.appsettings.ui.AppSettingsViewModel.Companion.DAYNIGHT_MODE_KEY
import com.boswelja.devicemanager.bootorupdate.BootOrUpdateHandlerService
import com.boswelja.devicemanager.bootorupdate.updater.Updater
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val nightMode = try {
            PreferenceManager.getDefaultSharedPreferences(this).getInt(
                DAYNIGHT_MODE_KEY,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        } catch (ignored: Exception) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
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

package com.boswelja.devicemanager

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        handleUpdates()
    }

    private fun handleUpdates() {
        if (EnvironmentUpdater(this).doUpdate() == EnvironmentUpdater.Result.COMPLETED) {
            PreferenceManager.getDefaultSharedPreferences(this).edit {
                putBoolean(SHOW_CHANGELOG_KEY, true)
            }
        }
    }

    companion object {
        const val SHOW_CHANGELOG_KEY = "should_show_changelog"
    }
}

package com.boswelja.devicemanager

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.updater.Result
import com.boswelja.devicemanager.updater.Updater

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        handleUpdates()
    }

    private fun handleUpdates() {
        if (Updater(this).doUpdate() == Result.COMPLETED) {
            PreferenceManager.getDefaultSharedPreferences(this).edit {
                putBoolean(SHOW_CHANGELOG_KEY, true)
            }
        }
    }

    companion object {
        const val SHOW_CHANGELOG_KEY = "should_show_changelog"
    }
}

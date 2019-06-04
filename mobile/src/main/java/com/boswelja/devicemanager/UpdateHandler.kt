package com.boswelja.devicemanager

import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.ui.version.ChangelogDialogFragment

class UpdateHandler(private val activity: AppCompatActivity) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

    init {
        try {
            ensureUpdated()
        } catch (ignored: ClassCastException) {
            sharedPreferences.edit().remove(APP_VERSION_KEY).apply()
            ensureUpdated()
        }
    }

    @Throws(ClassCastException::class)
    private fun ensureUpdated() {
        val oldVersion = sharedPreferences.getInt(APP_VERSION_KEY, 0)
        val currentVersion = BuildConfig.VERSION_CODE
        if (oldVersion < currentVersion) {
            ChangelogDialogFragment().show(activity.supportFragmentManager, "ChangelogDialog")
            sharedPreferences.edit().putInt(APP_VERSION_KEY, currentVersion).apply()
        }
        when {
            oldVersion < 120190605 -> {
                sharedPreferences.edit()
                        .remove("battery_phone_full_charge")
                        .remove("battery_watch_full_charge")
                        .apply()
            }
        }
    }

    companion object {
        private const val APP_VERSION_KEY = "app_version"
    }
}
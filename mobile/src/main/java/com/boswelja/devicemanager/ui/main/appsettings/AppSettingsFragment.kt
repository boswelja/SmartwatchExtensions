/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.appsettings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.managespace.ManageSpaceActivity
import com.boswelja.devicemanager.ui.base.BaseDayNightActivity.Companion.DAYNIGHT_MODE_KEY
import com.boswelja.devicemanager.ui.base.BaseWatchPickerPreferenceFragment
import com.boswelja.devicemanager.ui.widget.WidgetSettingsActivity
import timber.log.Timber

class AppSettingsFragment :
    BaseWatchPickerPreferenceFragment(),
    Preference.OnPreferenceClickListener {

    private lateinit var openNotiSettingsPreference: Preference
    private lateinit var daynightModePreference: ListPreference

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            OPEN_NOTI_SETTINGS_KEY -> {
                openNotificationSettings()
                true
            }
            OPEN_WATCH_MANAGER_KEY -> {
                activity.startWatchManagerActivity()
                true
            }
            OPEN_MANAGE_SPACE_KEY -> {
                openManageSpaceActivity()
                true
            }
            OPEN_WIDGET_SETTINGS_KEY -> {
                openWidgetSettingsActivity()
                true
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Timber.d("onCreatePreferences() called")
        addPreferencesFromResource(R.xml.prefs_app_settings)

        openNotiSettingsPreference = findPreference(OPEN_NOTI_SETTINGS_KEY)!!
        openNotiSettingsPreference.onPreferenceClickListener = this

        daynightModePreference = findPreference(DAYNIGHT_MODE_KEY)!!

        findPreference<Preference>(OPEN_WATCH_MANAGER_KEY)?.onPreferenceClickListener = this
        findPreference<Preference>(OPEN_MANAGE_SPACE_KEY)?.onPreferenceClickListener = this
        findPreference<Preference>(OPEN_WIDGET_SETTINGS_KEY)?.onPreferenceClickListener = this
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart() called")
        updateNotiSettingsPreferenceSummary()
        updateDayNightModePreferenceSummary()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop() called")
    }

    /**
     * Checks whether app notifications are allowed,
     * and updates [openNotiSettingsPreference] summary.
     */
    private fun updateNotiSettingsPreferenceSummary() {
        Timber.d("updateNotiSettingsPreferenceSummary() called")
        val notificationsAllowed = Compat.areNotificationsEnabled(requireContext())
        if (notificationsAllowed) {
            Timber.i("Notifications allowed")
            openNotiSettingsPreference.apply {
                summary = getString(R.string.pref_noti_status_summary_enabled)
                setIcon(R.drawable.pref_ic_notifications_enabled)
            }
        } else {
            Timber.i("Notifications disabled")
            openNotiSettingsPreference.apply {
                summary = getString(R.string.pref_noti_status_summary_disabled)
                setIcon(R.drawable.pref_ic_notifications_disabled)
            }
        }
    }

    /**
     * Updates [daynightModePreference] summary to reflect the current state of day/night mode.
     */
    private fun updateDayNightModePreferenceSummary() {
        daynightModePreference.summary = daynightModePreference.entry
    }

    /**
     * Opens the system's notification settings for Wearable Extensions.
     */
    private fun openNotificationSettings() {
        Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName!!)
            } else {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", context?.packageName!!)
                putExtra("app_uid", context?.applicationInfo?.uid!!)
            }
        }.also {
            Timber.i("Starting notification settings activity")
            startActivity(it)
        }
    }

    /**
     * Opens the [ManageSpaceActivity].
     */
    private fun openManageSpaceActivity() {
        Intent(requireContext(), ManageSpaceActivity::class.java).also {
            Timber.i("Starting ManageSpaceActivity")
            startActivity(it)
        }
    }

    /**
     * Opens the [WidgetSettingsActivity].
     */
    private fun openWidgetSettingsActivity() {
        Intent(requireContext(), WidgetSettingsActivity::class.java).also {
            Timber.i("Starting WidgetSettingsActivity")
            startActivity(it)
        }
    }

    companion object {
        const val SHOW_WIDGET_BACKGROUND_KEY = "show_widget_background"
        const val WIDGET_BACKGROUND_OPACITY_KEY = "widget_background_opacity"

        const val OPEN_NOTI_SETTINGS_KEY = "show_noti_settings"
        const val OPEN_WATCH_MANAGER_KEY = "open_watch_manager"
        const val OPEN_MANAGE_SPACE_KEY = "show_manage_space"
        const val OPEN_WIDGET_SETTINGS_KEY = "show_widget_settings"
    }
}

/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.UpdateHandler
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity

class MainActivity : BaseToolbarActivity() {

    private lateinit var settingsFragment: SettingsFragment
    private lateinit var sharedPrefs: SharedPreferences

    override fun getContentViewId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        UpdateHandler(this)

        super.onCreate(savedInstanceState)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        showSettingsFragment()
    }

    private fun showSettingsFragment() {
        settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()
        if (intent != null) {
            val key = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            if (key == PreferenceKey.PHONE_LOCKING_ENABLED_KEY ||
                    key == SettingsFragment.OPEN_NOTI_SETTINGS_KEY ||
                    key == SettingsFragment.DAYNIGHT_MODE_KEY ||
                    key == SettingsFragment.BATTERY_OPTIMISATION_STATUS_KEY) {
                settingsFragment.scrollToPreference(key)
            }
        }
    }
}

/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.BatteryUpdateJob
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.DnDLocalChangeListener
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseToolbarActivity() {

    private lateinit var settingsFragment: SettingsFragment
    private lateinit var sharedPrefs: SharedPreferences

    override fun getContentViewId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        showSettingsFragment()

        val battSyncEnabled = sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        if (battSyncEnabled) {
            if (Compat.getPendingJob(this, BatteryUpdateJob.BATTERY_PERCENT_JOB_ID) == null) {
                Utils.createBatterySyncJob(this)
            }
        } else {
            Utils.stopBatterySyncJob(this)
        }

        if (sharedPrefs.getBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY, false)) {
            val intent = Intent(this, DnDLocalChangeListener::class.java)
            Compat.startForegroundService(this, intent)
        }
    }

    private fun showSettingsFragment() {
        settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()

        if (intent != null) {
            val intentKey = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            if (!intentKey.isNullOrEmpty()) {
                settingsFragment.scrollToPreference(intentKey)
            }
        }
    }

    fun createSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.fragment_holder), message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_PREFERENCE_KEY = "extra_preference_key"
    }
}

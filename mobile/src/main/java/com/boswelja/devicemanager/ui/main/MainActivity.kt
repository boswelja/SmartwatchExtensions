/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.BatteryUpdateJob
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncLayer
import com.boswelja.devicemanager.receiver.DeviceAdminChangeReceiver
import com.boswelja.devicemanager.receiver.DeviceAdminChangeReceiver.Companion.DEVICE_ADMIN_ENABLED_KEY
import com.boswelja.devicemanager.service.DnDLocalChangeListener
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.batterysync.BatterySyncPreferenceActivity
import com.boswelja.devicemanager.ui.interruptfiltersync.InterruptFilterSyncPreferenceActivity
import com.boswelja.devicemanager.ui.version.ChangelogDialogFragment

class MainActivity : BaseToolbarActivity() {

    private lateinit var settingsFragment: SettingsFragment
    private lateinit var sharedPrefs: SharedPreferences

    override fun getContentViewId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        checkVersion()

        showSettingsFragment()

        val battSyncEnabled = sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        if (battSyncEnabled) {
            BatteryUpdateJob.startJob(this)
        } else {
            BatteryUpdateJob.stopJob(this)
        }

        if (sharedPrefs.getBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY, false)) {
            val intent = Intent(this, DnDLocalChangeListener::class.java)
            Compat.startForegroundService(this, intent)
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun checkVersion() {
        val oldVersion = sharedPrefs.getString(APP_VERSION_KEY, "")
        val currentVersion = getString(R.string.app_version_name)
        if (oldVersion.isNullOrBlank()) {
            val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val isDeviceAdminGranted = devicePolicyManager.isAdminActive(ComponentName(this, DeviceAdminChangeReceiver::class.java))
            sharedPrefs.edit()
                    .clear()
                    .putString(APP_VERSION_KEY, currentVersion)
                    .putBoolean(DEVICE_ADMIN_ENABLED_KEY, isDeviceAdminGranted)
                    .commit()
            PreferenceSyncLayer(this).pushNewData()
        }
        if (oldVersion != currentVersion) {
            ChangelogDialogFragment().show(supportFragmentManager, "ChangelogDialog")
            sharedPrefs.edit().putString(APP_VERSION_KEY, currentVersion).apply()
        }
    }

    private fun showSettingsFragment() {
        settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()
        if (intent != null) {
            val key = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            if (!key.isNullOrBlank()) {
                when (key) {
                    PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                    SettingsFragment.OPEN_NOTI_SETTINGS_KEY,
                    SettingsFragment.DAYNIGHT_MODE_KEY,
                    SettingsFragment.BATTERY_OPTIMISATION_STATUS_KEY -> {
                        settingsFragment.scrollToPreference(key)
                    }
                    PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
                    PreferenceKey.BATTERY_SYNC_INTERVAL_KEY,
                    PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY,
                    PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY -> {
                        val intent = Intent(this, BatterySyncPreferenceActivity::class.java)
                        intent.putExtra(EXTRA_PREFERENCE_KEY, key)
                        startActivity(intent)
                    }
                    PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY,
                    PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
                    PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                        val intent = Intent(this, InterruptFilterSyncPreferenceActivity::class.java)
                        intent.putExtra(EXTRA_PREFERENCE_KEY, key)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_PREFERENCE_KEY = "extra_preference_key"

        private const val APP_VERSION_KEY = "app_version"
    }
}

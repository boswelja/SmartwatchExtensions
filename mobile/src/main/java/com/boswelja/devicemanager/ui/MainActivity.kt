/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.BatteryUpdateJob
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.DnDLocalChangeListener
import com.boswelja.devicemanager.common.PreferenceKey
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var deviceAdminReceiver: ComponentName
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        AppCompatDelegate.setDefaultNightMode(sharedPrefs.getInt(PreferenceKey.DAYNIGHT_SWITCH_KEY, AppCompatDelegate.MODE_NIGHT_NO))

        setContentView(R.layout.activity_main)


        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        deviceAdminReceiver = DeviceAdminReceiver().getWho(this)

        settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()

        val battSyncEnabled = sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        if (battSyncEnabled) {
            if (Compat.getPendingJob(this, BatteryUpdateJob.BATTERY_PERCENT_JOB_ID) == null) {
                Utils.createBatterySyncJob(this)
            }
        } else {
            Utils.stopBatterySyncJob(this)
        }

        if (sharedPrefs.getBoolean(PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY, false)) {
            val intent = Intent(this, DnDLocalChangeListener::class.java)
            Compat.startForegroundService(this, intent)
        }
    }

    fun isDeviceAdmin(): Boolean {
        return devicePolicyManager.isAdminActive(deviceAdminReceiver)
    }

    fun changeAppIconVisibility(hide: Boolean) {
        val componentName = ComponentName(this, LauncherActivity::class.java)
        if (hide) {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        } else {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    fun createSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.fragment_holder), message, Snackbar.LENGTH_LONG).show()
    }
}

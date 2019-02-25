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
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.DnDLocalChangeListener
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.BatteryInfoUpdate
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var deviceAdminReceiver: ComponentName
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var jobScheduler: JobScheduler
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        AppCompatDelegate.setDefaultNightMode(prefs.getInt(PreferenceKey.DAYNIGHT_SWITCH_KEY, AppCompatDelegate.MODE_NIGHT_NO))

        setContentView(R.layout.activity_main)

        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        deviceAdminReceiver = DeviceAdminReceiver().getWho(this)

        settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()

        val battSyncEnabled = prefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        if (battSyncEnabled) {
            if (Compat.getPendingJob(jobScheduler, References.BATTERY_PERCENT_JOB_ID) == null) {
                createBatterySyncJob(prefs.getInt(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY, 900000).toLong())
            }
        } else {
            stopBatterySyncJob()
        }

        if (prefs.getBoolean(PreferenceKey.DND_SYNC_SEND_KEY, false)) {
            val intent = Intent(this, DnDLocalChangeListener::class.java)
            Compat.startService(this, intent)
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

    fun createBatterySyncJob(intervalMs: Long) {
        val jobInfo = JobInfo.Builder(References.BATTERY_PERCENT_JOB_ID, ComponentName(packageName, BatteryInfoUpdate::class.java.name))
        jobInfo.setPeriodic(intervalMs)
        jobInfo.setPersisted(true)
        jobScheduler.schedule(jobInfo.build())
    }

    fun stopBatterySyncJob() {
        if (Compat.getPendingJob(jobScheduler, References.BATTERY_PERCENT_JOB_ID) != null) {
            jobScheduler.cancel(References.BATTERY_PERCENT_JOB_ID)
        }
        val dataClient = Wearable.getDataClient(this)
        val putDataMapReq = PutDataMapRequest.create(References.BATTERY_PERCENT_KEY)
        if (putDataMapReq.dataMap.containsKey(References.BATTERY_PERCENT_PATH)) {
            putDataMapReq.dataMap.remove(References.BATTERY_PERCENT_PATH)
        }
        val putDataReq = putDataMapReq.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
    }

    fun createSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.fragment_holder), message, Snackbar.LENGTH_LONG).show()
    }
}

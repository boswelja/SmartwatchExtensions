/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.annotation.SuppressLint
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.DnDHandler
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

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        setContentView(R.layout.activity_main)

        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        deviceAdminReceiver = DeviceAdminReceiver().getWho(this)

        settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val battSyncEnabled = prefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        if (battSyncEnabled) {
            if (Compat.getPendingJob(jobScheduler, References.BATTERY_PERCENT_JOB_ID) == null) {
                createBatterySyncJob(prefs.getInt(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY, 900000).toLong())
            }
        } else {
            stopBatterySyncJob()
        }

        if (prefs.getBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, false) &&
                prefs.getBoolean(PreferenceKey.DND_SYNC_SEND_KEY, true)) {
            val intent = Intent(this, DnDHandler::class.java)
            Compat.startService(this, intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && prefs.getBoolean(PreferenceKey.CHECK_BATTERY_OPTIMISATION, true)) {
            val pwm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isOptimisingBattery = !pwm.isIgnoringBatteryOptimizations(packageName)
            if (isOptimisingBattery) {
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_battery_optimisation_warn_title))
                        .setMessage(getString(R.string.dialog_battery_optimisation_warn_message))
                        .setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                            run {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            }
                        }
                        .setNeutralButton(R.string.dialog_button_not_again) { _, _ -> prefs.edit().putBoolean(PreferenceKey.CHECK_BATTERY_OPTIMISATION, false).apply() }
                        .setNegativeButton(R.string.dialog_button_no) { dialog, _ -> dialog.dismiss() }
                        .show()
            }
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

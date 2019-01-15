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
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.boswelja.devicemanager.Compat
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.DnDHandler
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.tasks.BatteryInfoUpdate
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var deviceAdminReceiver: ComponentName
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var jobScheduler: JobScheduler

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                createBatterySyncJob(prefs.getString(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY, "600000")!!.toLong())
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
                        .setTitle("Battery Optimisation")
                        .setMessage("Android is optimising this app's battery use. This can cause issues with certain functions. Would you like to disable it?")
                        .setPositiveButton("Yes") { _, _ ->
                            run {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            }
                        }
                        .setNeutralButton("Don't ask again") { _, _ -> prefs.edit().putBoolean(PreferenceKey.CHECK_BATTERY_OPTIMISATION, false).apply() }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
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
        Log.d(tag, "Started battery sync with " + intervalMs + "ms interval")
    }

    fun stopBatterySyncJob() {
        if (Compat.getPendingJob(jobScheduler, References.BATTERY_PERCENT_JOB_ID) != null) {
            jobScheduler.cancel(References.BATTERY_PERCENT_JOB_ID)
            Log.d(tag, "Cancelled battery sync")
        }
        val dataClient = Wearable.getDataClient(this)
        val putDataMapReq = PutDataMapRequest.create(References.BATTERY_PERCENT_KEY)
        putDataMapReq.dataMap.remove(References.BATTERY_PERCENT_PATH)
        val putDataReq = putDataMapReq.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
    }
}

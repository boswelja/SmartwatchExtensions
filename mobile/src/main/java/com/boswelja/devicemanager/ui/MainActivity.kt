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
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.service.DnDSyncService
import com.boswelja.devicemanager.tasks.BatteryInfoUpdate
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var deviceAdminReceiver: ComponentName
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var jobScheduler: JobScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = FrameLayout(this)
        contentView.id = R.id.fragment_holder
        setContentView(contentView)

        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        deviceAdminReceiver = DeviceAdminReceiver().getWho(this)

        settingsFragment = SettingsFragment()
        fragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val battSyncEnabled = prefs.getBoolean(References.BATTERY_SYNC_ENABLED_KEY, false)
        if (battSyncEnabled) {
            if (jobScheduler.getPendingJob(References.BATTERY_PERCENT_JOB_ID) == null) {
                createBatterySyncJob(prefs.getString(References.BATTERY_SYNC_INTERVAL_KEY, "600000").toLong())
            }
        } else {
            stopBatterySyncJob()
        }

        val dndSyncEnabled = prefs.getBoolean(References.DND_SYNC_ENABLED_KEY, false)
        if (dndSyncEnabled) {
            val intent = Intent(this, DnDSyncService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            References.DEVICE_ADMIN_REQUEST_CODE -> {
                settingsFragment.updateAdminSummary()
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
        Log.d("MainActivity", "Started battery sync with " + intervalMs + "ms interval")
    }

    fun stopBatterySyncJob() {
        if (jobScheduler.getPendingJob(References.BATTERY_PERCENT_JOB_ID) != null) {
            jobScheduler.cancel(References.BATTERY_PERCENT_JOB_ID)
            Log.d("MainActivity", "Cancelled battery sync")
        }
        val dataClient = Wearable.getDataClient(this)
        val putDataMapReq = PutDataMapRequest.create("/batteryPercent")
        putDataMapReq.dataMap.remove("com.boswelja.devicemanager.batterypercent")
        val putDataReq = putDataMapReq.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
    }
}

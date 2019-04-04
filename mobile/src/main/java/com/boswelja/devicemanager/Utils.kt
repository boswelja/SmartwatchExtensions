/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.BatteryUpdateJob
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.MainActivity

object Utils {

    fun requestDeviceAdminPerms(context: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminReceiver().getWho(context))
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.device_admin_desc))
        context.startActivity(intent)
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(intent)
    }

    fun switchDayNightMode(activity: MainActivity) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val currentNightMode = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                Log.d("switchDayNightMode", "Night mode off, switching on")
                prefs.edit().putInt(PreferenceKey.DAYNIGHT_SWITCH_KEY, AppCompatDelegate.MODE_NIGHT_YES).apply()
            }
            else -> {
                prefs.edit().putInt(PreferenceKey.DAYNIGHT_SWITCH_KEY, AppCompatDelegate.MODE_NIGHT_NO).apply()
            }
        }
        activity.recreate()
    }

    fun createBatterySyncJob(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        Utils.createBatterySyncJob(context, prefs.getInt(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY, 900000).toLong())
    }

    fun createBatterySyncJob(context: Context, intervalMs: Long) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(BatteryUpdateJob.BATTERY_PERCENT_JOB_ID, ComponentName(context.packageName, BatteryUpdateJob::class.java.name))
        jobInfo.setPeriodic(intervalMs)
        jobInfo.setPersisted(true)
        jobScheduler.schedule(jobInfo.build())
    }

    fun stopBatterySyncJob(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        if (Compat.getPendingJob(jobScheduler, BatteryUpdateJob.BATTERY_PERCENT_JOB_ID) != null) {
            jobScheduler.cancel(BatteryUpdateJob.BATTERY_PERCENT_JOB_ID)
        }
    }
}
/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.batterysync.Utils.updateBatteryStats

class BatteryUpdateJob : JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        updateBatteryStats(this, References.CAPABILITY_WATCH_APP)
        jobFinished(params, true)
        return false
    }

    companion object {

        private const val BATTERY_UPDATE_JOB_ID = 5656299

        fun startJob(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            startJob(context, prefs.getInt(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY, 900000).toLong())
        }

        fun startJob(context: Context, intervalMs: Long) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val jobInfo = JobInfo.Builder(
                    BATTERY_UPDATE_JOB_ID,
                    ComponentName(context.packageName, BatteryUpdateJob::class.java.name)).apply {
                setPeriodic(intervalMs)
                setPersisted(true)
            }
            jobScheduler.schedule(jobInfo.build())
        }

        fun stopJob(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            if (Compat.getPendingJob(jobScheduler, BATTERY_UPDATE_JOB_ID) != null) {
                jobScheduler.cancel(BATTERY_UPDATE_JOB_ID)
            }
        }
    }
}

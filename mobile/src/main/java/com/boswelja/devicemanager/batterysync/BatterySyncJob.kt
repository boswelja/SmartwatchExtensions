/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.ui.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BatterySyncJob : JobService() {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            if (params?.jobId != null) {
                MainScope().launch {
                    val watchId = service.getWatchByBatterySyncJobId(params?.jobId!!)?.id
                    updateBatteryStats(this@BatterySyncJob, watchId)
                }
            }

            unbindService(this)
        }

        override fun onWatchManagerUnbound() {
            jobFinished(params, true)
        }
    }

    private var params: JobParameters? = null

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        this.params = params
        WatchConnectionService.bind(this, watchConnectionManagerConnection)
        return false
    }

    companion object {

        /**
         * Starts a battery sync job for the currently selected watch.
         * @return @`true` if the job was started successfully, @`false` otherwise.
         */
        fun startJob(watchConnectionManager: WatchConnectionService?): Boolean {
            val connectedWatch = watchConnectionManager?.getConnectedWatch()
            if (connectedWatch != null) {
                val jobId = connectedWatch.batterySyncJobId

                val syncIntervalMinutes = connectedWatch.intPrefs[BATTERY_SYNC_INTERVAL_KEY] ?: 15
                val syncIntervalMillis = TimeUnit.MINUTES.toMillis(syncIntervalMinutes.toLong())

                val jobScheduler = watchConnectionManager.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val jobInfo = JobInfo.Builder(
                        jobId,
                        ComponentName(watchConnectionManager.packageName, BatterySyncJob::class.java.name)).apply {
                    setPeriodic(syncIntervalMillis)
                    setPersisted(true)
                }
                return jobScheduler.schedule(jobInfo.build()) == JobScheduler.RESULT_SUCCESS
            }
            return false
        }

        /**
         * Stops the battery sync job for the current watch.
         */
        fun stopJob(watchConnectionManager: WatchConnectionService?) {
            val connectedWatch = watchConnectionManager?.getConnectedWatch()
            if (connectedWatch != null) {
                val jobId = connectedWatch.batterySyncJobId
                val jobScheduler = watchConnectionManager.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                if (Compat.getPendingJob(jobScheduler, jobId) != null) {
                    jobScheduler.cancel(jobId)
                }
            }
        }
    }
}

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
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.ui.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class BatterySyncJob : JobService() {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            if (params?.jobId != null) {
                val watchId = service.getWatchIdFor(JOB_ID_KEY, params?.jobId!!)
                updateBatteryStats(this@BatterySyncJob, watchId)
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

        private const val JOB_ID_KEY = "job_id_key"

        /**
         * Starts a battery sync job for the currently selected watch.
         * @return @`true` if the job was started successfully, @`false` otherwise.
         */
        fun startJob(context: Context, watchConnectionManager: WatchConnectionService?): Boolean {
            val connectedWatch = watchConnectionManager?.getConnectedWatch()
            if (connectedWatch != null) {
                var needsUpdate = false
                val jobId = if (connectedWatch.intPrefs.contains(JOB_ID_KEY)) {
                    connectedWatch.intPrefs[JOB_ID_KEY]!!
                } else {
                    needsUpdate = true
                    Random.nextInt(100000, 999999)
                }
                if (needsUpdate) {
                    watchConnectionManager.updatePrefInDatabase(JOB_ID_KEY, jobId)
                }

                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val syncIntervalMinutes = prefs.getInt(BATTERY_SYNC_INTERVAL_KEY, 15).toLong()
                val syncIntervalMillis = TimeUnit.MINUTES.toMillis(syncIntervalMinutes)

                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val jobInfo = JobInfo.Builder(
                        jobId,
                        ComponentName(context.packageName, BatterySyncJob::class.java.name)).apply {
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
        fun stopJob(context: Context, watchConnectionManager: WatchConnectionService?) {
            val connectedWatch = watchConnectionManager?.getConnectedWatch()
            if (connectedWatch != null) {
                if (connectedWatch.intPrefs.contains(JOB_ID_KEY)) {
                    val jobId = connectedWatch.intPrefs[JOB_ID_KEY]!!
                    val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    if (Compat.getPendingJob(jobScheduler, jobId) != null) {
                        jobScheduler.cancel(jobId)
                    }
                }
            }
        }
    }
}

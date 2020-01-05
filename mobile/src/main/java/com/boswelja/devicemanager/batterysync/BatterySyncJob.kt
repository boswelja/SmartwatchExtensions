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
import android.os.PersistableBundle
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.ui.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BatterySyncJob : JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        if (params != null) {
            val watchId = params.extras.getString(EXTRA_WATCH_ID)
            if (watchId != null) {
                updateBatteryStats(this, watchId)
            }
        }
        return false
    }

    companion object {

        private const val EXTRA_WATCH_ID: String = "extra_watch_id"

        /**
         * Starts a battery sync job for the currently selected watch.
         * @return @`true` if the job was started successfully, @`false` otherwise.
         */
        suspend fun startJob(watchConnectionManager: WatchConnectionService?): Boolean {
            return withContext(Dispatchers.Default) {
                val connectedWatch = watchConnectionManager?.getConnectedWatch()
                if (connectedWatch != null) {
                    val jobId = connectedWatch.batterySyncJobId

                    val syncIntervalMinutes = connectedWatch.intPrefs[BATTERY_SYNC_INTERVAL_KEY] ?: 15

                    return@withContext startJob(watchConnectionManager, connectedWatch.id, syncIntervalMinutes, jobId)
                }
                false
            }
        }

        fun startJob(context: Context, watchId: String, syncIntervalMinutes: Int, jobId: Int): Boolean {
            val syncIntervalMillis = TimeUnit.MINUTES.toMillis(syncIntervalMinutes.toLong())

            val extras = PersistableBundle()
            extras.putString(EXTRA_WATCH_ID, watchId)

            val jobInfo = JobInfo.Builder(
                    jobId,
                    ComponentName(context.packageName, BatterySyncJob::class.java.name)).apply {
                setPeriodic(syncIntervalMillis)
                setPersisted(true)
                setExtras(extras)
            }
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            return jobScheduler.schedule(jobInfo.build()) == JobScheduler.RESULT_SUCCESS
        }

        /**
         * Stops the battery sync job for the current watch.
         */
        suspend fun stopJob(watchConnectionManager: WatchConnectionService?) {
            withContext(Dispatchers.IO) {
                val connectedWatch = watchConnectionManager?.getConnectedWatch()
                withContext(Dispatchers.Default) {
                    if (connectedWatch != null) {
                        val jobId = connectedWatch.batterySyncJobId
                        stopJob(watchConnectionManager, jobId)
                    }
                }
            }
        }

        fun stopJob(context: Context, jobId: Int) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            if (Compat.getPendingJob(jobScheduler, jobId) != null) {
                jobScheduler.cancel(jobId)
            }
        }
    }
}

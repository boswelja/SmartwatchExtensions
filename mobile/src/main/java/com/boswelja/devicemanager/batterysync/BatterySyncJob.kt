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
import androidx.room.Room
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.ui.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.boswelja.devicemanager.watchconnectionmanager.WatchDatabase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatterySyncJob : JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        if (params?.jobId != null) {
            MainScope().launch {
                withContext(Dispatchers.IO) {
                    val database = Room.databaseBuilder(applicationContext, WatchDatabase::class.java, "watch-db")
                            .fallbackToDestructiveMigration()
                            .build()
                    val watchId = database.watchDao().findByBatterySyncJobId(params.jobId)?.id
                    updateBatteryStats(this@BatterySyncJob, watchId)
                }
                jobFinished(params, true)
            }
        }
        return true
    }

    companion object {

        /**
         * Starts a battery sync job for the currently selected watch.
         * @return @`true` if the job was started successfully, @`false` otherwise.
         */
        suspend fun startJob(watchConnectionManager: WatchConnectionService?): Boolean {
            return withContext(Dispatchers.IO) {
                val connectedWatch = watchConnectionManager?.getConnectedWatch()
                return@withContext withContext(Dispatchers.Default) {
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
                        return@withContext jobScheduler.schedule(jobInfo.build()) == JobScheduler.RESULT_SUCCESS
                    }
                    false
                }
            }
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
                        val jobScheduler = watchConnectionManager.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                        if (Compat.getPendingJob(jobScheduler, jobId) != null) {
                            jobScheduler.cancel(jobId)
                        }
                    }
                }
            }
        }
    }
}

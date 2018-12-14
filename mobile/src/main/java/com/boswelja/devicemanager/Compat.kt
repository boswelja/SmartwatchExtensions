package com.boswelja.devicemanager

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.os.Build

object Compat {

    fun getPendingJob(jobScheduler: JobScheduler, id: Int) : JobInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobScheduler.getPendingJob(id)
        } else {
            val jobs = jobScheduler.allPendingJobs
            jobs.first { j -> j.id == id }
        }
    }

    fun startService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
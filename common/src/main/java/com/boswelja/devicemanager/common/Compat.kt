/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.preference.PreferenceManager

/*
 * Compatibility layer to aid support for different Android versions
 */
@SuppressLint("ObsoleteSdkInt")
object Compat {

    /**
     * Finds pending jobs from {@link JobScheduler} matching the given ID.
     * @param id ID of the job to find.
     * @return JobInfo object, null if not found.
     * @see JobInfo
     */
    fun getPendingJob(context: Context, id: Int): JobInfo? {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        return Compat.getPendingJob(jobScheduler, id)
    }

    /**
     * Finds pending jobs from {@link JobScheduler} matching the given ID.
     * @param id ID of the job to find.
     * @return JobInfo object, null if not found.
     * @see JobInfo
     */
    fun getPendingJob(jobScheduler: JobScheduler, id: Int): JobInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobScheduler.getPendingJob(id)
        } else {
            val jobs = jobScheduler.allPendingJobs
            jobs.firstOrNull { j -> j.id == id }
        }
    }

    /**
     * Starts a service in the foreground.
     * @param intent Intent for the service.
     */
    fun startForegroundService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Set the system's current Interruption Filter state, or set silent mode if
     * Interruption Filter doesn't exist.
     * @param interruptionFilterOn Specify the new Interruption Filter state.
     */
    fun setInterruptionFilter(context: Context, interruptionFilterOn: Boolean) {
        if (interruptionFilterOn != interruptionFilterEnabled(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (interruptionFilterOn) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    } else {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                    val isPhone = context.resources.getBoolean(R.bool.deviceIsPhone)
                    val receivingKey = if (isPhone) {
                        PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
                    } else {
                        PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY
                    }
                    prefs.edit().putBoolean(receivingKey, false).apply()
                }
            } else {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (interruptionFilterOn) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                } else {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
            }
        }
    }

    /**
     * Checks whether or not Interruption Filter is currently active, or check silent
     * mode state if Interruption Filter is unavailable.
     * @return Whether or not Interruption Filter is enabled.
     */
    fun interruptionFilterEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val currentInterruptFilter = notificationManager.currentInterruptionFilter
            (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) ||
                    (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) ||
                    (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
        } else {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        }
    }
}
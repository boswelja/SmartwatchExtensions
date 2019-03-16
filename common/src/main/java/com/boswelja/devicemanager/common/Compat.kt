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
import android.preference.PreferenceManager

@SuppressLint("ObsoleteSdkInt")
object Compat {

    fun getPendingJob(jobScheduler: JobScheduler, id: Int): JobInfo? {
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

    fun setInterruptionFilter(context: Context, requestedDnDState: Boolean) {
        if (requestedDnDState != dndEnabled(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (requestedDnDState) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    } else {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                    prefs.edit().putBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, false).apply()
                }
            } else {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (requestedDnDState) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                } else {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
            }
        }
    }

    private fun dndEnabled(context: Context): Boolean {
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
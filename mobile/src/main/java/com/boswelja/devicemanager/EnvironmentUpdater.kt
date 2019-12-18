/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.BatterySyncJob
import com.boswelja.devicemanager.batterysync.WatchBatteryUpdateReceiver
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.dndsync.References
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnvironmentUpdater(private val context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val currentAppVersion: Int = BuildConfig.VERSION_CODE
    private val lastAppVersion: Int

    private var notificationChannelsCreated: Boolean = false

    init {
        lastAppVersion = sharedPreferences.getInt(APP_VERSION_KEY, currentAppVersion)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelsCreated = sharedPreferences.getBoolean(NOTIFICATION_CHANNELS_CREATED, false)
        }
    }

    private fun needsUpdate(): Boolean {
        return lastAppVersion < currentAppVersion
    }

    private fun doBatterySyncUpdate(watch: Watch) {
        if (lastAppVersion < 2019120600) {
            val oldJobId = sharedPreferences.getInt("job_id_key", 0)
            if (oldJobId != 0) {
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                if (Compat.getPendingJob(jobScheduler, oldJobId) != null) {
                    jobScheduler.cancel(oldJobId)
                }

                val syncIntervalMinutes = sharedPreferences.getInt(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY, 15).toLong()
                val syncIntervalMillis = TimeUnit.MINUTES.toMillis(syncIntervalMinutes)

                val jobInfo = JobInfo.Builder(
                        watch.batterySyncJobId,
                        ComponentName(context.packageName, BatterySyncJob::class.java.name)).apply {
                    setPeriodic(syncIntervalMillis)
                    setPersisted(true)
                }
                jobScheduler.schedule(jobInfo.build())
            }
            sharedPreferences.edit().remove("job_id_key").apply()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)!!

        if (notificationManager.getNotificationChannel(WatchBatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                    WatchBatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANNEL_ID,
                    context.getString(R.string.noti_channel_watch_charged_title),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(false)
                enableVibration(true)
                setShowBadge(true)
            }.also {
                notificationManager.createNotificationChannel(it)
            }
        }

        if (notificationManager.getNotificationChannel(References.DND_SYNC_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                    References.DND_SYNC_NOTI_CHANNEL_ID,
                    context.getString(R.string.noti_channel_dnd_sync_title),
                    NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }.also {
                notificationManager.createNotificationChannel(it)
            }
        }
    }

    fun doUpdate(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (!notificationChannelsCreated or needsUpdate())) {
            createNotificationChannels()
            sharedPreferences.edit().putBoolean(NOTIFICATION_CHANNELS_CREATED, true).apply()
            notificationChannelsCreated = true
        }

        if (needsUpdate()) {
            var updateStatus = UPDATE_NOTHING_CHANGED
            sharedPreferences.edit(commit = true) {
                if (lastAppVersion < 2019090243) {
                    remove("connected_watch_name")
                    updateStatus = UPDATE_SUCCESS
                }
                if (lastAppVersion < 2019110999) {
                    val lastConnectedId = sharedPreferences.getString("connected_watch_id", "")
                    remove("connected_watch_id")
                    putString("last_connected_id", lastConnectedId)
                    updateStatus = UPDATE_SUCCESS
                }
                if (lastAppVersion < 2019120600) {
                    updateStatus = NEEDS_FULL_UPDATE
                }
                putInt(APP_VERSION_KEY, lastAppVersion)
            }
            return updateStatus
        }
        return UPDATE_NOTHING_CHANGED
    }

    suspend fun doFullUpdate(watchConnectionManager: WatchConnectionService) {
        if (lastAppVersion < 2019120600) {
            withContext(Dispatchers.IO) {
                val capableNodes = Tasks.await(Wearable.getCapabilityClient(context)
                        .getCapability(CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL))
                        .nodes
                val defaultWatch = capableNodes.firstOrNull { it.isNearby } ?: capableNodes.firstOrNull()
                if (defaultWatch != null) {
                    val watch = Watch(defaultWatch)
                    watchConnectionManager.addWatch(watch)
                    watchConnectionManager.setConnectedWatchById(watch.id)
                    sharedPreferences.all.forEach {
                        if (it.value != null) {
                            watchConnectionManager.updatePrefInDatabase(it.key, it.value!!)
                        }
                    }
                    withContext(Dispatchers.Default) {
                        doBatterySyncUpdate(watch)
                    }
                }
            }
        }
    }

    companion object {
        private const val APP_VERSION_KEY = "app_version"
        private const val NOTIFICATION_CHANNELS_CREATED = "notification_channels_created"

        const val UPDATE_NOTHING_CHANGED = 0
        const val UPDATE_SUCCESS = 1
        const val NEEDS_FULL_UPDATE = 2
    }
}

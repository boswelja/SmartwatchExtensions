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
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.BatterySyncJob
import com.boswelja.devicemanager.batterysync.WatchBatteryUpdateReceiver
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.dndsync.References
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
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

    private suspend fun doBatterySyncUpdate(watchConnectionManager: WatchConnectionService) {
        val watches = watchConnectionManager.getRegisteredWatches()
        var needsRestart = false
        if (lastAppVersion < 2019120600) {
            val watch = watches[0]
            if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
                val oldJobId = sharedPreferences.getInt("job_id_key", 0)
                if (oldJobId != 0) {
                    BatterySyncJob.stopJob(context, oldJobId)
                    needsRestart = true
                }
                sharedPreferences.edit().remove("job_id_key").apply()
            }
        } else if (lastAppVersion < 2020010500) {
            withContext(Dispatchers.IO) {
                for (watch in watches) {
                    if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
                        BatterySyncJob.stopJob(context, watch.batterySyncJobId)
                        needsRestart = true
                    }
                }
            }
        }

        if (needsRestart) {
            for (watch in watches) {
                if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
                    BatterySyncJob.startJob(context, watch.id, watch.intPrefs[BATTERY_SYNC_INTERVAL_KEY] ?: 15, watch.batterySyncJobId)
                }
            }
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
                            when (it.key) {
                                PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                                BATTERY_SYNC_ENABLED_KEY,
                                PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                                PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
                                PreferenceKey.DND_SYNC_TO_PHONE_KEY,
                                PreferenceKey.DND_SYNC_TO_WATCH_KEY,
                                PreferenceKey.DND_SYNC_WITH_THEATER_KEY,
                                PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                                    watchConnectionManager.updatePrefInDatabase(it.key, it.value!!)
                                }
                            }
                        }
                    }
                    withContext(Dispatchers.Default) {
                        doBatterySyncUpdate(watchConnectionManager)
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

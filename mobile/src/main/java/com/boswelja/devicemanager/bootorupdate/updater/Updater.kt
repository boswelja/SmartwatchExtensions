/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.bootorupdate.updater

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.batterysync.WatchBatteryUpdateReceiver
import com.boswelja.devicemanager.bootorupdate.BootOrUpdateHandlerService
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.dndsync.References
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.ui.phonelocking.PhoneLockingPreferenceFragment.Companion.PHONE_LOCKING_MODE_KEY
import com.boswelja.devicemanager.watchmanager.Utils
import com.boswelja.devicemanager.watchmanager.Watch
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class Updater(private val context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val currentAppVersion: Int = BuildConfig.VERSION_CODE
    private val lastAppVersion: Int

    private val needsUpdate: Boolean get() = lastAppVersion < currentAppVersion

    private var notificationChannelsCreated: Boolean = false

    init {
        lastAppVersion = try {
            sharedPreferences.getInt(APP_VERSION_KEY, currentAppVersion)
        } catch (e: ClassCastException) {
            Timber.w("Failed to get lat app version, falling back to version 0 to force upgrade")
            0
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelsCreated = sharedPreferences.getBoolean(NOTIFICATION_CHANNELS_CREATED, false)
        }
    }

    /**
     * Restarts all [BatterySyncWorker] instances.
     * @param database The [WatchDatabase] to get worker IDs from.
     */
    private fun doBatterySyncUpdate(database: WatchDatabase) {
        var needsRestart = false
        if (sharedPreferences.contains("job_id_key")) {
            sharedPreferences.edit().remove("job_id_key").apply()
        }
        if (lastAppVersion < 2020041100) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancelAll()
            needsRestart = true
        }

        if (needsRestart) {
            val watches = database.getRegisteredWatchesWithPrefs()
            if (watches != null) {
                for (watch in watches) {
                    if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
                        val newWorkerId = BatterySyncWorker.startWorker(context, watch.id, (watch.intPrefs[BATTERY_SYNC_INTERVAL_KEY] ?: 15).toLong())
                        database.watchDao().updateBatterySyncWorkerId(watch.id, newWorkerId)
                    }
                }
            }
        }
    }

    /**
     * Creates all notifications channels if they don't already exist.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        if (!notificationChannelsCreated || needsUpdate) {
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

            if (notificationManager.getNotificationChannel(BootOrUpdateHandlerService.BOOT_OR_UPDATE_NOTI_CHANNEL_ID) == null) {
                NotificationChannel(
                        BootOrUpdateHandlerService.BOOT_OR_UPDATE_NOTI_CHANNEL_ID,
                        context.getString(R.string.noti_channel_boot_or_update_title),
                        NotificationManager.IMPORTANCE_LOW).apply {
                    enableLights(false)
                    enableVibration(false)
                    setShowBadge(false)
                }.also {
                    notificationManager.createNotificationChannel(it)
                }
            }

            sharedPreferences.edit().putBoolean(NOTIFICATION_CHANNELS_CREATED, true).apply()
            notificationChannelsCreated = true
        }
    }

    /**
     * Update the app's working environment.
     * @return The [Result] of the update
     */
    fun doUpdate(): Result {
        var updateStatus = Result.NOT_NEEDED
        if (needsUpdate) {
            sharedPreferences.edit(commit = true) {
                if (lastAppVersion < 2019090243) {
                    remove("connected_watch_name")
                    updateStatus = Result.COMPLETED
                }
                if (lastAppVersion < 2019110999) {
                    val lastConnectedId = sharedPreferences.getString("connected_watch_id", "")
                    remove("connected_watch_id")
                    putString("last_connected_id", lastConnectedId)
                    updateStatus = Result.COMPLETED
                }
                if (lastAppVersion < 2020011400) {
                    remove("battery_sync_last_when")
                    remove("battery_sync_enabled")
                    updateStatus = Result.COMPLETED
                }
                if (lastAppVersion < 2019120600) {
                    doFullUpdate()
                    updateStatus = Result.COMPLETED
                }
                if (lastAppVersion < 2020020200) {
                    remove("ignore_battery_opt_warning")
                    remove("ignore_watch_charge_warning")
                }
                if (lastAppVersion < 2020040600) {
                    MessageDatabase.open(context).apply {
                        updateMessageCount(sharedPreferences)
                    }.also {
                        it.close()
                    }
                }
                if (lastAppVersion < 2020040700) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        putString(PHONE_LOCKING_MODE_KEY, "1")
                    } else {
                        putString(PHONE_LOCKING_MODE_KEY, "0")
                    }
                }
                if (lastAppVersion < 2020042200) {
                    remove("has_completed_first_run")
                }
                putInt(APP_VERSION_KEY, lastAppVersion)
            }
        }
        return updateStatus
    }

    /**
     * Performs an update on databases as necessary.
     */
    private fun doFullUpdate() {
        if (lastAppVersion < 2019120600) {
            val messageClient = Wearable.getMessageClient(context)
            val database = WatchDatabase.open(context, allowMainThreadQueries = true)

            val capableNodes = Tasks.await(Wearable.getCapabilityClient(context)
                    .getCapability(CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL))
                    .nodes
            val defaultWatch = capableNodes.firstOrNull { it.isNearby } ?: capableNodes.firstOrNull()

            if (defaultWatch != null) {
                val watch = Watch(defaultWatch)
                Utils.addWatch(database, messageClient, watch)
                sharedPreferences.edit {
                    putString(WatchManager.LAST_CONNECTED_NODE_ID_KEY, watch.id)
                }
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
                                database.updatePrefInDatabase(watch.id, it.key, it.value!!)
                            }
                        }
                    }
                }
                doBatterySyncUpdate(database)
            }
            database.close()
        }
    }

    companion object {
        private const val APP_VERSION_KEY = "app_version"
        private const val NOTIFICATION_CHANNELS_CREATED = "notification_channels_created"
    }
}

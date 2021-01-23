/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.bootorupdate.updater

import android.app.job.JobScheduler
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.preference.PreferenceKey
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.setup.References
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingPreferenceFragment.Companion.PHONE_LOCKING_MODE_KEY
import com.boswelja.devicemanager.watchmanager.SelectedWatchHandler.Companion.LAST_SELECTED_NODE_ID_KEY
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class Updater(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private val currentAppVersion: Int = BuildConfig.VERSION_CODE
    private val lastAppVersion: Int

    val needsUpdate: Boolean
        get() = lastAppVersion < currentAppVersion

    init {
        lastAppVersion =
            try {
                sharedPreferences.getInt(APP_VERSION_KEY, currentAppVersion)
            } catch (e: ClassCastException) {
                Timber.w(
                    "Failed to get lat app version, falling back to version 0 to force upgrade"
                )
                0
            }
    }

    /**
     * Restarts all [BatterySyncWorker] instances.
     * @param database The [WatchDatabase] to get worker IDs from.
     */
    private suspend fun doBatterySyncUpdate(database: WatchDatabase) {
        var needsRestart = false
        if (sharedPreferences.contains("job_id_key")) {
            sharedPreferences.edit().remove("job_id_key").apply()
        }
        if (lastAppVersion < 2020041100) {
            val jobScheduler =
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancelAll()
            needsRestart = true
        }

        if (needsRestart) {
            val watchesWithWorkers =
                database
                    .boolPrefDao()
                    .getAllForKey(BATTERY_SYNC_ENABLED_KEY)
                    .filter { it.value }
                    .map { it.watchId }
            for (watchId in watchesWithWorkers) {
                BatterySyncWorker.startWorker(context, watchId)
            }
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
                if (sharedPreferences.contains("connected_watch_name")) {
                    remove("connected_watch_name")
                    updateStatus = Result.COMPLETED
                }
                if (sharedPreferences.contains("connected_watch_id")) {
                    val lastConnectedId = sharedPreferences.getString("connected_watch_id", "")
                    remove("connected_watch_id")
                    putString("last_connected_id", lastConnectedId)
                    updateStatus = Result.COMPLETED
                }
                if (sharedPreferences.contains("battery_sync_last_when")) {
                    remove("battery_sync_last_when")
                    updateStatus = Result.COMPLETED
                }
                if (sharedPreferences.contains("ignore_battery_opt_warning")) {
                    remove("ignore_battery_opt_warning")
                    updateStatus = Result.COMPLETED
                }
                if (sharedPreferences.contains("ignore_watch_charge_warning")) {
                    remove("ignore_watch_charge_warning")
                    updateStatus = Result.COMPLETED
                }
                if (sharedPreferences.contains("auto_add_watches")) {
                    remove("auto_add_watches")
                    updateStatus = Result.COMPLETED
                }
                if (sharedPreferences.contains("should_show_changelog")) {
                    remove("should_show_changelog")
                    updateStatus = Result.COMPLETED
                }
                if (sharedPreferences.contains("message_count")) {
                    remove("message_count")
                    updateStatus = Result.COMPLETED
                }
                if (!sharedPreferences.contains(PHONE_LOCKING_MODE_KEY)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        putString(PHONE_LOCKING_MODE_KEY, "1")
                    } else {
                        putString(PHONE_LOCKING_MODE_KEY, "0")
                    }
                }
                if (sharedPreferences.contains("has_completed_first_run")) {
                    remove("has_completed_first_run")
                }
                if (sharedPreferences.contains("notification_channels_created")) {
                    remove("notification_channels_created")
                }
                if (lastAppVersion < 2020072700) {
                    WatchBatteryWidget.enableWidget(context)
                    updateStatus = Result.COMPLETED
                }
                if (lastAppVersion < 2019120600) {
                    doFullUpdate()
                    updateStatus = Result.COMPLETED
                }
                putInt(APP_VERSION_KEY, lastAppVersion)
            }
        }
        return updateStatus
    }

    /** Performs an update on databases as necessary. */
    private fun doFullUpdate() {
        Wearable.getCapabilityClient(context)
            .getCapability(CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)
            .addOnSuccessListener { capabilityInfo ->
                coroutineScope.launch {
                    val messageClient = Wearable.getMessageClient(context)
                    val database = WatchDatabase.get(context)

                    val capableNodes = capabilityInfo.nodes
                    val defaultWatch =
                        capableNodes.firstOrNull { it.isNearby } ?: capableNodes.firstOrNull()

                    if (defaultWatch != null) {
                        val watch = Watch(defaultWatch)
                        coroutineScope.launch { database.watchDao().add(watch) }
                        messageClient.sendMessage(watch.id, References.WATCH_REGISTERED_PATH, null)
                        sharedPreferences.edit { putString(LAST_SELECTED_NODE_ID_KEY, watch.id) }
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
                }
            }
    }

    companion object {
        private const val APP_VERSION_KEY = "app_version"
    }
}

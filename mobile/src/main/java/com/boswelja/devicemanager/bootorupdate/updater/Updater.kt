@file:Suppress("DEPRECATION")

package com.boswelja.devicemanager.bootorupdate.updater

import android.app.job.JobScheduler
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.appsettings.ui.AppSettingsViewModel.Companion.DAYNIGHT_MODE_KEY
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget.Companion.WIDGET_BACKGROUND_OPACITY_KEY
import com.boswelja.devicemanager.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.connection.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.preference.PreferenceKey
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingSettingsViewModel.Companion.PHONE_LOCKING_MODE_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager.Companion.LAST_SELECTED_NODE_ID_KEY
import com.boswelja.devicemanager.watchmanager.connection.wearos.WearOSConnectionInterface
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import com.boswelja.devicemanager.widget.widgetIdStore
import com.boswelja.devicemanager.widget.widgetSettings
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class Updater(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private val currentAppVersion: Int = BuildConfig.VERSION_CODE
    private val lastAppVersion: Int = try {
        sharedPreferences.getInt(APP_VERSION_KEY, currentAppVersion)
    } catch (e: ClassCastException) {
        Timber.w(
            "Failed to get lat app version, falling back to version 0 to force upgrade"
        )
        0
    }

    val needsUpdate: Boolean
        get() = lastAppVersion < currentAppVersion

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
                if (sharedPreferences.contains(SHOW_WIDGET_BACKGROUND_KEY.name)) {
                    val showBackground = sharedPreferences
                        .getBoolean(SHOW_WIDGET_BACKGROUND_KEY.name, true)
                    runBlocking {
                        context.widgetSettings.edit {
                            it[SHOW_WIDGET_BACKGROUND_KEY] = showBackground
                        }
                    }
                    remove(SHOW_WIDGET_BACKGROUND_KEY.name)
                }
                if (sharedPreferences.contains(WIDGET_BACKGROUND_OPACITY_KEY.name)) {
                    val backgroundOpacity = sharedPreferences
                        .getInt(WIDGET_BACKGROUND_OPACITY_KEY.name, 60)
                    runBlocking {
                        context.widgetSettings.edit {
                            it[WIDGET_BACKGROUND_OPACITY_KEY] = backgroundOpacity
                        }
                    }
                    remove(WIDGET_BACKGROUND_OPACITY_KEY.name)
                }
                if (lastAppVersion < 2026800000) {
                    val value = sharedPreferences.getString(
                        DAYNIGHT_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
                    )
                    putInt(
                        DAYNIGHT_MODE_KEY,
                        value?.toInt() ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                }
                if (lastAppVersion < 2025600000) {
                    Analytics().setAnalyticsEnabled(
                        sharedPreferences.getBoolean(Analytics.ANALYTICS_ENABLED_KEY, false)
                    )
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
            if (lastAppVersion < 2027000000) {
                runBlocking { updateWidgetImpl() }
                updateStatus = Result.COMPLETED
            }
        }
        return updateStatus
    }

    private suspend fun updateWidgetImpl() {
        val widgetIdStore = context.widgetIdStore
        Room.databaseBuilder(context, WidgetDatabase::class.java, "widget-db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build().also { database ->
                widgetIdStore.edit { widgetIds ->
                    database.widgetDao().getAll().forEach {
                        widgetIds[stringPreferencesKey(it.widgetId.toString())] = it.watchId
                    }
                }
                database.clearAllTables()
                database.close()
            }
    }

    /** Performs an update on databases as necessary. */
    private fun doFullUpdate() {
        Wearable.getCapabilityClient(context)
            .getCapability(CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)
            .addOnSuccessListener { capabilityInfo ->
                coroutineScope.launch {
                    val messageClient = Wearable.getMessageClient(context)
                    val database = WatchDatabase.getInstance(context)

                    val capableNodes = capabilityInfo.nodes
                    val defaultWatch =
                        capableNodes.firstOrNull { it.isNearby } ?: capableNodes.firstOrNull()

                    if (defaultWatch != null) {
                        val watch = Watch(
                            defaultWatch.id,
                            defaultWatch.displayName,
                            WearOSConnectionInterface.PLATFORM
                        )
                        coroutineScope.launch { database.watchDao().add(watch) }
                        messageClient.sendMessage(watch.id, WATCH_REGISTERED_PATH, null)
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

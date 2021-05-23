package com.boswelja.smartwatchextensions.watchmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.boswelja.smartwatchextensions.AppState
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appStateStore
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.common.SingletonHolder
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.common.connection.Messages
import com.boswelja.smartwatchextensions.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.smartwatchextensions.common.connection.Preference.toByteArray
import com.boswelja.smartwatchextensions.watchmanager.database.DbWatch.Companion.toDbWatch
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.smartwatchextensions.watchmanager.item.Preference
import com.boswelja.smartwatchextensions.widget.widgetIdStore
import com.boswelja.watchconnection.core.MessageListener
import com.boswelja.watchconnection.core.Watch
import com.boswelja.watchconnection.core.WatchPlatformManager
import com.boswelja.watchconnection.wearos.WearOSPlatform
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Provides a simplified interface for interacting with all watch-related classes.
 */
class WatchManager internal constructor(
    val settingsDatabase: WatchSettingsDatabase,
    private val watchDatabase: WatchDatabase,
    private val connectionClient: WatchPlatformManager,
    private val analytics: Analytics,
    private val dataStore: DataStore<AppState>,
    private val coroutineScope: CoroutineScope
) {

    constructor(context: Context) : this(
        WatchSettingsDatabase.getInstance(context),
        WatchDatabase.getInstance(context),
        WatchPlatformManager(
            WearOSPlatform(context, CAPABILITY_WATCH_APP, Capability.values().map { it.name })
        ),
        Analytics(),
        context.appStateStore,
        CoroutineScope(Dispatchers.IO)
    )

    private val _selectedWatchId = MutableLiveData<UUID>(null)
    private val _selectedWatch = _selectedWatchId.switchMap { id ->
        if (id != null) watchDatabase.getById(id)
        else liveData { }
    }

    val registeredWatches: LiveData<List<Watch>>
        get() = watchDatabase.watchDao().getAllObservable().map { it }

    @ExperimentalCoroutinesApi
    val availableWatches: Flow<List<Watch>>
        get() = connectionClient.watchesWithApp()
            .map { watches ->
                watches.filter { watchDatabase.watchDao().get(it.id) == null }
            }

    /**
     * The currently selected watch
     */
    val selectedWatch: LiveData<Watch?> = _selectedWatch.map { it }

    init {
        Timber.d("Creating WatchManager")
        // Set the initial selectedWatch value if possible.
        coroutineScope.launch {
            dataStore.data.map { it.lastSelectedWatchId }.collect {
                if (it.isNotBlank()) selectWatchById(UUID.fromString(it))
                else {
                    Timber.w("No watch previously selected")
                    registeredWatches.asFlow().first().firstOrNull()?.let { watch ->
                        selectWatchById(watch.id)
                    }
                }
            }
        }
    }

    suspend fun registerWatch(watch: Watch) {
        withContext(Dispatchers.IO) {
            connectionClient.sendMessage(watch, Messages.WATCH_REGISTERED_PATH)
            watchDatabase.watchDao().add(watch.toDbWatch())
            analytics.logWatchRegistered()
        }
    }

    suspend fun forgetWatch(context: Context, watch: Watch) {
        forgetWatch(
            context.widgetIdStore,
            WatchBatteryStatsDatabase.getInstance(context),
            watch
        )
    }

    internal suspend fun forgetWatch(
        widgetIdStore: DataStore<Preferences>,
        batteryStatsDatabase: WatchBatteryStatsDatabase,
        watch: Watch
    ) {
        withContext(Dispatchers.IO) {
            batteryStatsDatabase.batteryStatsDao().deleteStatsForWatch(watch.id)
            connectionClient.sendMessage(watch, Messages.RESET_APP)
            watchDatabase.removeWatch(watch)
            removeWidgetsForWatch(watch, widgetIdStore)
            analytics.logWatchRemoved()
        }
    }

    suspend fun renameWatch(watch: Watch, newName: String) {
        withContext(Dispatchers.IO) {
            watchDatabase.renameWatch(watch, newName)
            analytics.logWatchRenamed()
        }
    }

    suspend fun resetWatchPreferences(context: Context, watch: Watch) {
        resetWatchPreferences(
            context.widgetIdStore,
            WatchBatteryStatsDatabase.getInstance(context),
            watch
        )
    }

    internal suspend fun resetWatchPreferences(
        widgetIdStore: DataStore<Preferences>,
        batteryStatsDatabase: WatchBatteryStatsDatabase,
        watch: Watch
    ) {
        batteryStatsDatabase.batteryStatsDao().deleteStatsForWatch(watch.id)
        connectionClient.sendMessage(watch, CLEAR_PREFERENCES)
        settingsDatabase.clearWatchPreferences(watch)
        removeWidgetsForWatch(watch, widgetIdStore)
    }

    internal suspend fun removeWidgetsForWatch(
        watch: Watch,
        widgetIdStore: DataStore<Preferences>
    ) {
        val widgetsToRemove = widgetIdStore.data.firstOrNull()?.asMap()
            ?.filter { it.value == watch.id }?.keys
        if (!widgetsToRemove.isNullOrEmpty()) {
            widgetIdStore.edit { mutablePreferences ->
                widgetsToRemove.forEach {
                    mutablePreferences.remove(it)
                }
            }
        }
    }

    /**
     * Selects a watch by a given [Watch.id]. This will update [selectedWatch].
     * @param watchId The ID of the [Watch] to select.
     */
    fun selectWatchById(watchId: UUID) {
        Timber.d("selectWatchById(%s) called", watchId)
        _selectedWatchId.postValue(watchId)
        coroutineScope.launch {
            dataStore.updateData { settings ->
                settings.copy(lastSelectedWatchId = watchId.toString())
            }
        }
    }

    fun getStatusFor(watch: Watch) = connectionClient.getStatusFor(watch)

    fun getCapabilitiesFor(watch: Watch) = connectionClient.getCapabilitiesFor(watch)

    suspend fun sendMessage(watch: Watch, message: String, data: ByteArray? = null) =
        connectionClient.sendMessage(watch, message, data)

    /**
     * Gets a preference for a given watch with a specified key.
     * @param watchId See [Watch.id].
     * @param key The [Preference.key] of the preference to find.
     * @return The value of the preference, or null if it doesn't exist.
     */
    suspend inline fun <reified T> getPreference(watchId: UUID, key: String) =
        settingsDatabase.getPreference<T>(watchId, key)?.value

    /**
     * Gets a preference for a given watch with a specified key, wrapped in a LiveData.
     * @param watchId See [Watch.id].
     * @param key The [Preference.key] of the preference to find.
     * @return The value of the preference, or null if it doesn't exist.
     */
    inline fun <reified T> getPreferenceObservable(watchId: UUID, key: String) =
        settingsDatabase.getPreferenceObservable<T>(watchId, key)?.map { it?.value } ?: liveData { }

    /**
     * Update a preference for a given watch.
     * @param watch The [Watch] to send the preference update to.
     * @param key The preference key to update.
     * @param value The new preference value.
     */
    suspend fun updatePreference(watch: Watch, key: String, value: Any) {
        Timber.d("updatePreference(%s, %s, %s) called", watch.toString(), key, value.toString())
        val message = when (value) {
            is Boolean -> Messages.UPDATE_BOOL_PREFERENCE
            is Int -> Messages.UPDATE_INT_PREFERENCE
            else -> throw IllegalArgumentException()
        }
        withContext(Dispatchers.IO) {
            connectionClient.sendMessage(
                watch,
                message,
                Pair(key, value).toByteArray()
            )
            settingsDatabase.updatePrefInDatabase(watch.id, key, value)
            analytics.logExtensionSettingChanged(key, value)
        }
    }

    /**
     * Update a preference for all registered watches.
     * @param key The preference key to update.
     * @param value The new preference value.
     */
    suspend fun updatePreference(key: String, value: Boolean) {
        watchDatabase.watchDao().getAll().forEach {
            updatePreference(it, key, value)
        }
    }

    suspend fun getWatchById(id: UUID) = watchDatabase.watchDao().get(id)
    fun observeWatchById(id: UUID): LiveData<Watch?> =
        watchDatabase.watchDao().getObservable(id).map { it }

    fun registerMessageListener(messageListener: MessageListener) =
        connectionClient.addMessageListener(messageListener)
    fun unregisterMessageListener(messageListener: MessageListener) =
        connectionClient.removeMessageListener(messageListener)

    companion object : SingletonHolder<WatchManager, Context>(::WatchManager) {
        const val CAPABILITY_WATCH_APP = "extensions_watch_app"
    }
}

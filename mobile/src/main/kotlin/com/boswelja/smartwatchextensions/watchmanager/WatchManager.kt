package com.boswelja.smartwatchextensions.watchmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.boswelja.smartwatchextensions.AppState
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appStateStore
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.common.SingletonHolder
import com.boswelja.smartwatchextensions.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_UPDATE_CAPABILITIES
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.smartwatchextensions.watchmanager.item.Preference
import com.boswelja.smartwatchextensions.watchmanager.item.Watch
import com.boswelja.smartwatchextensions.widget.widgetIdStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Provides a simplified interface for interacting with [WatchRepository], as well as maintaining
 * the selected watch state.
 */
class WatchManager internal constructor(
    private val watchRepository: WatchRepository,
    val settingsDatabase: WatchSettingsDatabase,
    private val analytics: Analytics,
    private val dataStore: DataStore<AppState>,
    private val coroutineScope: CoroutineScope
) {

    constructor(context: Context) : this(
        WatchRepository(context),
        WatchSettingsDatabase.getInstance(context),
        Analytics(),
        context.appStateStore,
        CoroutineScope(Dispatchers.IO)
    )

    private val _selectedWatchId = MutableLiveData("")
    private val _selectedWatch = MediatorLiveData<Watch?>()

    val registeredWatches: LiveData<List<Watch>>
        get() = watchRepository.registeredWatches
    val availableWatches: LiveData<List<Watch>>
        get() = watchRepository.availableWatches

    /**
     * The currently selected watch
     */
    val selectedWatch: LiveData<Watch?>
        get() = _selectedWatch

    init {
        Timber.d("Creating WatchManager")
        _selectedWatch.addSource(registeredWatches) {
            Timber.d("registeredWatches changed, updating _selectedWatch")
            val watch = it.firstOrNull { watch ->
                watch.id == _selectedWatchId.value
            } ?: it.firstOrNull()
            _selectedWatch.value = watch
        }
        _selectedWatch.addSource(_selectedWatchId) {
            val watch = registeredWatches.value?.firstOrNull { watch -> watch.id == it }
            if (watch == null) {
                Timber.w("Tried to select a watch with id $it, but it wasn't registered")
                registeredWatches.value?.firstOrNull()?.let { fallbackWatch ->
                    _selectedWatch.value = fallbackWatch
                }
            } else {
                _selectedWatch.value = watch
            }
        }

        refreshData()

        // Set the initial selectedWatch value if possible.
        coroutineScope.launch {
            dataStore.data.map { it.lastSelectedWatchId }.collect {
                if (it.isNotBlank()) selectWatchById(it)
                else {
                    Timber.w("No watch previously selected")
                    registeredWatches.value?.firstOrNull()?.let { watch ->
                        selectWatchById(watch.id)
                    }
                }
            }
        }
    }

    suspend fun registerWatch(watch: Watch) {
        watchRepository.registerWatch(watch)
        analytics.logWatchRegistered()
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
            watchRepository.resetWatch(watch)
            removeWidgetsForWatch(watch, widgetIdStore)
            analytics.logWatchRemoved()
        }
    }

    suspend fun renameWatch(watch: Watch, newName: String) {
        watchRepository.renameWatch(watch, newName)
        analytics.logWatchRenamed()
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
        watchRepository.sendMessage(watch, CLEAR_PREFERENCES)
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
    fun selectWatchById(watchId: String) {
        Timber.d("selectWatchById(%s) called", watchId)
        _selectedWatchId.postValue(watchId)
        coroutineScope.launch {
            dataStore.updateData { settings ->
                settings.copy(lastSelectedWatchId = watchId)
            }
        }
    }

    fun requestRefreshCapabilities(watch: Watch) {
        watchRepository.sendMessage(watch, REQUEST_UPDATE_CAPABILITIES)
    }

    fun refreshData() = watchRepository.refreshData()

    fun sendMessage(watch: Watch, messagePath: String, data: ByteArray? = null) =
        watchRepository.sendMessage(watch, messagePath, data)

    /**
     * Gets a preference for a given watch with a specified key.
     * @param watchId See [Watch.id].
     * @param key The [Preference.key] of the preference to find.
     * @return The value of the preference, or null if it doesn't exist.
     */
    suspend inline fun <reified T> getPreference(watchId: String, key: String) =
        settingsDatabase.getPreference<T>(watchId, key)?.value

    /**
     * Gets a preference for a given watch with a specified key, wrapped in a LiveData.
     * @param watchId See [Watch.id].
     * @param key The [Preference.key] of the preference to find.
     * @return The value of the preference, or null if it doesn't exist.
     */
    inline fun <reified T> getPreferenceObservable(watchId: String, key: String) =
        settingsDatabase.getPreferenceObservable<T>(watchId, key)?.map { it?.value } ?: liveData { }

    /**
     * Update a preference for a given watch.
     * @param watch The [Watch] to send the preference update to.
     * @param key The preference key to update.
     * @param value The new preference value.
     */
    suspend fun updatePreference(watch: Watch, key: String, value: Any) {
        watchRepository.updatePreferenceOnWatch(watch, key, value)
        settingsDatabase.updatePrefInDatabase(watch.id, key, value)
        analytics.logExtensionSettingChanged(key, value)
    }

    /**
     * Update a preference for all registered watches.
     * @param key The preference key to update.
     * @param value The new preference value.
     */
    suspend fun updatePreference(key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDatabase.boolPrefDao().updateAllForKey(key, value)
            registeredWatches.value?.forEach {
                watchRepository.updatePreferenceOnWatch(it, key, value)
            }
            analytics.logExtensionSettingChanged(key, value)
        }
    }

    companion object : SingletonHolder<WatchManager, Context>(::WatchManager)
}

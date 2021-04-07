package com.boswelja.devicemanager.watchmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.AppState
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.appStateStore
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.common.SingletonHolder
import com.boswelja.devicemanager.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.devicemanager.common.connection.Messages.REQUEST_UPDATE_CAPABILITIES
import com.boswelja.devicemanager.watchmanager.database.WatchSettingsDatabase
import com.boswelja.devicemanager.watchmanager.item.Preference
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.widget.widgetIdStore
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

        _selectedWatch.addSource(registeredWatches) {
            val watch = it.firstOrNull { watch -> watch.id == _selectedWatchId.value }
            _selectedWatch.postValue(watch)
        }
        _selectedWatch.addSource(_selectedWatchId) {
            val watch = registeredWatches.value?.firstOrNull { watch -> watch.id == it }
            if (watch == null) {
                Timber.w("Tried to select a watch with id $it, but it wasn't registered")
            } else {
                _selectedWatch.postValue(watch)
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
     * @param watch The [Watch] to get the preference for.
     * @param key The [Preference.key] of the preference to find.
     * @return The value of the preference, or null if it doesn't exist.
     */
    suspend inline fun <reified T> getPreference(watch: Watch, key: String): T? {
        return withContext(Dispatchers.IO) {
            return@withContext settingsDatabase.getPreference<T>(watch, key)?.value
        }
    }

    fun updatePreference(watch: Watch, key: String, value: Any) {
        watchRepository.updatePreferenceOnWatch(watch, key, value)
        settingsDatabase.updatePrefInDatabase(watch.id, key, value)
        analytics.logExtensionSettingChanged(key, value)
    }

    companion object : SingletonHolder<WatchManager, Context>(::WatchManager)
}

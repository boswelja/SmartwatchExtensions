package com.boswelja.devicemanager.watchmanager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.common.SingletonHolder
import com.boswelja.devicemanager.common.connection.Messages.REQUEST_UPDATE_CAPABILITIES
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.widget.widgetIdStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Provides a simplified interface for interacting with [WatchRepository], as well as maintaining
 * the selected watch state.
 */
class WatchManager internal constructor(
    private val sharedPreferences: SharedPreferences,
    val watchRepository: WatchRepository,
    private val analytics: Analytics,
    private val coroutineScope: CoroutineScope
) {

    constructor(context: Context) : this(
        PreferenceManager.getDefaultSharedPreferences(context),
        WatchRepository(context),
        Analytics(),
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
        sharedPreferences.getString(LAST_SELECTED_NODE_ID_KEY, "")?.let {
            selectWatchById(it)
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
                updateLocalPreferences(watch)
            }
        }
    }

    /**
     * Clears all local [SharedPreferences], then reads the values stored in the database for a
     * specified [Watch] into the local [SharedPreferences].
     * @param watch The [Watch] to query the database for corresponding preferences.
     */
    private fun updateLocalPreferences(watch: Watch) {
        Timber.d("updateLocalPreferences($watch) called")
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                sharedPreferences.edit(commit = true) {
                    SyncPreferences.ALL_PREFS.forEach { remove(it) }
                }
                val prefs = watchRepository.getAllPreferences(watch)
                sharedPreferences.edit {
                    prefs.forEach {
                        Timber.i("Setting ${it.key} to ${it.value}")
                        when (it.value) {
                            is Int -> putInt(it.key, it.value)
                            is Boolean -> putBoolean(it.key, it.value)
                            else -> Timber.w("Unsupported preference type")
                        }
                    }
                    putString(LAST_SELECTED_NODE_ID_KEY, watch.id)
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
        watchRepository.resetWatchPreferences(watch)
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
        _selectedWatchId.postValue(watchId)
    }

    fun requestRefreshCapabilities(watch: Watch) {
        watchRepository.sendMessage(watch, REQUEST_UPDATE_CAPABILITIES)
    }

    fun refreshData() = watchRepository.refreshData()

    fun sendMessage(watch: Watch, messagePath: String, data: ByteArray? = null) =
        watchRepository.sendMessage(watch, messagePath, data)

    suspend inline fun <reified T> getPreference(watch: Watch, key: String) =
        watchRepository.getPreference<T>(watch, key)

    suspend fun updatePreference(watch: Watch, key: String, value: Any) {
        watchRepository.updatePreference(watch, key, value)
        analytics.logExtensionSettingChanged(key, value)
    }

    companion object : SingletonHolder<WatchManager, Context>(::WatchManager) {
        const val LAST_SELECTED_NODE_ID_KEY = "last_connected_id"
    }
}

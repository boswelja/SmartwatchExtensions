/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.common.SingletonHolder
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        Analytics(context),
        CoroutineScope(Dispatchers.IO)
    )

    private val _selectedWatch = MutableLiveData<Watch?>()

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

    suspend fun forgetWatch(watch: Watch): Boolean {
        val success = false // watchRepository.clearPreferencesForWatch(watch.id)
        if (success) {
            watchRepository.forgetWatch(watch)
            analytics.logWatchRemoved()
        }
        return success
    }

    suspend fun renameWatch(watch: Watch, newName: String) {
        watchRepository.renameWatch(watch, newName)
    }

    fun requestResetWatch(watch: Watch) {
        watchRepository.resetWatch(watch)
    }

    fun resetWatchPreferences(watch: Watch) {
        watchRepository.resetWatchPreferences(watch)
    }

    /**
     * Selects a watch by a given [Watch.id]. This will update [selectedWatch].
     * @param watchId The ID of the [Watch] to select.
     */
    fun selectWatchById(watchId: String) {
        // Make sure the new selected watch is different from the current selection
        if (watchId != _selectedWatch.value?.id) {
            val newWatch = registeredWatches.value?.firstOrNull { it.id == watchId }
            if (newWatch == null) {
                Timber.w("Tried to select a watch with id $watchId, but it wasn't registered")
                return
            }
            Timber.d("Setting connected watch to $watchId")
            _selectedWatch.postValue(newWatch)
            updateLocalPreferences(newWatch)
        }
    }

    suspend inline fun <reified T> getPreference(watch: Watch, key: String) =
        watchRepository.getPreference<T>(watch, key)

    suspend fun updatePreference(watch: Watch, key: String, value: Any) =
        watchRepository.updatePreference(watch, key, value)

    companion object : SingletonHolder<WatchManager, Context>(::WatchManager) {
        const val LAST_SELECTED_NODE_ID_KEY = "last_connected_id"
    }
}

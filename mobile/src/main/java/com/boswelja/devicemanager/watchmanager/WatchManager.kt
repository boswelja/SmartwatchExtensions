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
 * Provides a simplified interface for managing registered watches, finding new watches and updating
 * local SharedPreferences when the connected watch changes.
 */
class WatchManager internal constructor(
    private val sharedPreferences: SharedPreferences,
    private val selectedWatchHandler: SelectedWatchHandler,
    private val watchRepository: WatchRepository,
    private val analytics: Analytics,
    private val coroutineScope: CoroutineScope
) {

    constructor(context: Context) : this(
        PreferenceManager.getDefaultSharedPreferences(context),
        SelectedWatchHandler(context),
        WatchRepository(context),
        Analytics(context),
        CoroutineScope(Dispatchers.IO)
    )

    val registeredWatches: LiveData<List<Watch>>
        get() = watchRepository.registeredWatches
    val availableWatches: LiveData<List<Watch>>
        get() = watchRepository.availableWatches

    val connectedWatch: LiveData<Watch?>
        get() = selectedWatchHandler.selectedWatch

    init {
        connectedWatch.observeForever {
            it?.let { watch -> updateLocalPreferences(watch) }
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

    suspend fun <T> getPreference(watch: Watch, key: String) =
        watchRepository.getPreference<T>(watch, key)

    suspend fun updatePreference(watch: Watch, key: String, value: Any) =
        watchRepository.updatePreference(watch, key, value)

    companion object : SingletonHolder<WatchManager, Context>(::WatchManager)
}

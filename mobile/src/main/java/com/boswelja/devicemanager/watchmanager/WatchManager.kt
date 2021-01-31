/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.content.Context
import androidx.lifecycle.LiveData
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.common.SingletonHolder
import com.boswelja.devicemanager.watchmanager.item.Watch

/**
 * Provides a simplified interface for managing registered watches, finding new watches and updating
 * local SharedPreferences when the connected watch changes.
 */
class WatchManager internal constructor(
    private val watchPreferenceManager: WatchPreferenceManager,
    private val selectedWatchHandler: SelectedWatchHandler,
    private val watchRepository: WatchRepository,
    private val analytics: Analytics
) {

    constructor(context: Context) : this(
        WatchPreferenceManager.get(context),
        SelectedWatchHandler(context),
        WatchRepository(context),
        Analytics(context)
    )

    val registeredWatches: LiveData<List<Watch>>
        get() = watchRepository.registeredWatches
    val availableWatches: LiveData<List<Watch>>
        get() = watchRepository.availableWatches

    val connectedWatch: LiveData<Watch?>
        get() = selectedWatchHandler.selectedWatch

    init {
        connectedWatch.observeForever {
            it?.let { watch -> watchPreferenceManager.updateLocalPreferences(watch.id) }
        }
    }

    suspend fun registerWatch(watch: Watch) {
        watchRepository.registerWatch(watch)
        analytics.logWatchRegistered()
    }

    suspend fun forgetWatch(watch: Watch): Boolean {
        val success = watchPreferenceManager.clearPreferencesForWatch(watch.id)
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

    suspend fun updatePreference(watch: Watch, key: String, value: Any) =
        watchRepository.updatePreference(watch, key, value)

    companion object : SingletonHolder<WatchManager, Context>(::WatchManager)
}

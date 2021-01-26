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
import com.boswelja.devicemanager.watchmanager.communication.WearOSConnectionManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A singleton that keeps track of and updates the currently selected watch. Changes can be observed
 * via [selectedWatch]. Use [SelectedWatchHandler.get] to get an instance.
 */
class SelectedWatchHandler
internal constructor(
    context: Context,
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val connectionManager: WearOSConnectionManager = WearOSConnectionManager(context),
    val database: WatchDatabase = WatchDatabase.get(context)
) {

    private val _selectedWatch = MutableLiveData<Watch?>()
    private val _status = MutableLiveData(Watch.Status.UNKNOWN)

    val selectedWatch: LiveData<Watch?>
        get() = _selectedWatch
    val status: LiveData<Watch.Status>
        get() = _status

    init {
        // Set the initial connectedWatch value if possible.
        sharedPreferences.getString(LAST_SELECTED_NODE_ID_KEY, "")?.let {
            selectWatchById(it)
        }
    }

    /**
     * Selects a watch by a given [Watch.id]. This will update [selectedWatch].
     * @param watchId The ID of the [Watch] to select.
     */
    fun selectWatchById(watchId: String) {
        if (watchId != _selectedWatch.value?.id) {
            Timber.d("Setting connected watch to $watchId")
            coroutineScope.launch {
                val newWatch = database.watchDao().get(watchId)
                if (newWatch == null) {
                    Timber.w("Tried to select a watch with id $watchId, but it wasn't registered")
                    return@launch
                }
                _selectedWatch.postValue(newWatch)
                sharedPreferences.edit { putString(LAST_SELECTED_NODE_ID_KEY, newWatch.id) }
                _status.postValue(connectionManager.getWatchStatus(newWatch, true))
            }
        }
    }

    /**
     * Update [status] for the currently selected watch.
     */
    fun refreshStatus() {
        _selectedWatch.value?.let {
            _status.postValue(connectionManager.getWatchStatus(it, true))
        }
    }

    companion object {
        const val LAST_SELECTED_NODE_ID_KEY = "last_connected_id"

        private var INSTANCE: SelectedWatchHandler? = null

        /** Get an instance of [SelectedWatchHandler] */
        fun get(context: Context): SelectedWatchHandler {
            if (INSTANCE != null) return INSTANCE!!
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = SelectedWatchHandler(context)
                }
                return INSTANCE!!
            }
        }
    }
}

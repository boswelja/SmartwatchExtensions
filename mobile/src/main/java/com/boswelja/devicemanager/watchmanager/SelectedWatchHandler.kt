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
import com.boswelja.devicemanager.watchmanager.Utils.getWatchStatus
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
    val database: WatchDatabase = WatchDatabase.get(context)
) {

    private val _selectedWatch = MutableLiveData<Watch?>()
    private val _status = MutableLiveData(WatchStatus.UNKNOWN)

    val selectedWatch: LiveData<Watch?>
        get() = _selectedWatch
    val status: LiveData<WatchStatus>
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
                _selectedWatch.postValue(newWatch)
                sharedPreferences.edit { putString(LAST_SELECTED_NODE_ID_KEY, newWatch?.id) }
            }
            refreshStatus(watchId)
        }
    }

    /**
     * Update [status] for the currently selected watch.
     */
    private fun refreshStatus(watchId: String) {
        coroutineScope.launch {
            val newStatus = getWatchStatus(watchId, database)
            _status.postValue(newStatus)
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

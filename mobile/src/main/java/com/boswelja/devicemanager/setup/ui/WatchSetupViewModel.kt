/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.setup.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.common.Event
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WatchSetupViewModel @JvmOverloads constructor(
    application: Application,
    private val watchManager: WatchManager = WatchManager.get(application)
) : AndroidViewModel(application) {

    private val _availableWatches = MutableLiveData<List<Watch>?>(null)
    val availableWatches: LiveData<List<Watch>?>
        get() = _availableWatches

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    val newWatchRegistered = Event()

    init {
        refreshAvailableWatches()
    }

    fun refreshAvailableWatches() {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val availableWatches = watchManager.getAvailableWatches()
            _availableWatches.postValue(availableWatches)
            _isLoading.postValue(false)
        }
    }

    fun registerWatch(watch: Watch) {
        viewModelScope.launch(Dispatchers.IO) {
            watchManager.registerWatch(watch)
            newWatchRegistered.fire()
        }
    }
}

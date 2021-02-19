/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A ViewModel for handling the discovery and registration of watches.
 */
class RegisterWatchViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val dispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    private val _registeredWatches = ArrayList<Watch>()
    private val _registeredWatchesLive = MutableLiveData<List<Watch>>(emptyList())

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        Dispatchers.IO
    )

    val registeredWatches: LiveData<List<Watch>>
        get() = _registeredWatchesLive

    val availableWatches: LiveData<List<Watch>>
        get() = watchManager.availableWatches

    fun registerWatch(watch: Watch) {
        Timber.d("registerWatch($watch) called")
        viewModelScope.launch(dispatcher) {
            watchManager.registerWatch(watch)
            addToRegistered(watch)
        }
    }

    fun refreshData() {
        watchManager.refreshData()
    }

    private fun addToRegistered(watch: Watch) {
        _registeredWatches.add(watch)
        _registeredWatchesLive.postValue(_registeredWatches)
    }
}

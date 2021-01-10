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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A ViewModel for handling the discovery and registration of watches.
 */
class RegisterWatchViewModel internal constructor(
    application: Application,
    autoRegisterOnCreate: Boolean,
    private val watchManager: WatchManager = WatchManager.get(application)
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(
        application: Application
    ) : this(application, true)

    var registeredWatches: List<Watch> = emptyList()

    private val _isWorking = MutableLiveData(false)

    /**
     * Notifies observers when the ViewModel is or isn't working (i.e. registering watches)
     */
    val isWorking: LiveData<Boolean>
        get() = _isWorking

    init {
        if (autoRegisterOnCreate) registerAvailableWatches()
    }

    /**
     * Gets a list of all watches that are connected, not registered and have Wearable Extensions
     * installed, and registers each one.
     */
    fun registerAvailableWatches() {
        Timber.d("registerAvailableWatches() called")
        viewModelScope.launch(Dispatchers.IO) {
            suspendRegisterAvailableWatches()
        }
    }

    internal suspend fun suspendRegisterAvailableWatches() {
        _isWorking.postValue(true)
        val availableWatches = watchManager.getAvailableWatches()
        if (availableWatches != null) {
            availableWatches.forEach { watch ->
                watchManager.registerWatch(watch)
            }
            registeredWatches = availableWatches
        }
        _isWorking.postValue(false)
    }
}

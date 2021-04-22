package com.boswelja.smartwatchextensions.watchmanager.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A ViewModel for handling the discovery and registration of watches.
 */
@ExperimentalCoroutinesApi
class RegisterWatchViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val dispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        Dispatchers.IO
    )

    private val _addedWatches = MutableStateFlow<Watch?>(null)
    private val _watchesToAdd = MediatorLiveData<List<Watch>>()

    val registeredWatches: Flow<Watch>
        get() = _addedWatches.mapNotNull { it }

    val watchesToAdd: LiveData<List<Watch>>
        get() = _watchesToAdd

    init {
        startRegisteringWatches()
    }

    @ExperimentalCoroutinesApi
    fun startRegisteringWatches() {
        viewModelScope.launch {
            watchManager.availableWatches.collect {
                addWatch(it)
            }
        }
    }

    fun addWatch(watch: Watch) {
        Timber.d("registerWatch($watch) called")
        viewModelScope.launch(dispatcher) {
            watchManager.registerWatch(watch)
            _addedWatches.emit(watch)
        }
    }
}

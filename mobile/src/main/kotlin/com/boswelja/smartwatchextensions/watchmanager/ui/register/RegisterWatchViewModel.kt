package com.boswelja.smartwatchextensions.watchmanager.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A ViewModel for handling the discovery and registration of watches.
 */
@ExperimentalCoroutinesApi
class RegisterWatchViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )

    private val _addedWatches = MutableSharedFlow<Watch?>()

    val registeredWatches: Flow<Watch>
        get() = _addedWatches.mapNotNull { it }

    init {
        startRegisteringWatches()
    }

    @ExperimentalCoroutinesApi
    fun startRegisteringWatches() {
        viewModelScope.launch {
            watchManager.availableWatches.collectLatest { watches ->
                watches.forEach { watch -> addWatch(watch) }
            }
        }
    }

    suspend fun addWatch(watch: Watch) {
        Timber.d("registerWatch($watch) called")
        if (!_addedWatches.replayCache.contains(watch)) {
            _addedWatches.emit(watch)
            watchManager.registerWatch(watch)
        }
    }
}

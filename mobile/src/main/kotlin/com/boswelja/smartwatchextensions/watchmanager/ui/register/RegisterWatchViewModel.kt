package com.boswelja.smartwatchextensions.watchmanager.ui.register

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
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

    var addedWatches = mutableStateListOf<Watch>()

    init {
        startRegisteringWatches()
    }

    @ExperimentalCoroutinesApi
    fun startRegisteringWatches() {
        viewModelScope.launch {
            watchManager.availableWatches.collect { watches ->
                Timber.d("Got %s watches", watches.count())
                watches.forEach { watch ->
                    Timber.i("Adding watch %s", watch.id)
                    addWatch(watch)
                }
            }
        }
    }

    suspend fun addWatch(watch: Watch) {
        Timber.d("registerWatch($watch) called")
        if (!addedWatches.contains(watch)) {
            addedWatches.add(watch)
            watchManager.registerWatch(watch)
        }
    }
}

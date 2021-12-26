package com.boswelja.smartwatchextensions.devicemanagement.ui.register

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.launch

/**
 * A ViewModel for handling the discovery and registration of watches.
 */
class RegisterWatchViewModel(
    private val watchManager: WatchManager
) : ViewModel() {

    /**
     * A list of watches that have been discovered and registered so far.
     */
    var discoveredWatches = mutableStateListOf<Watch>()

    init {
        startRegisteringWatches()
    }

    private fun startRegisteringWatches() {
        viewModelScope.launch {
            watchManager.availableWatches.collect { watches ->
                watches.forEach { watch ->
                    addWatch(watch)
                }
            }
        }
    }

    private suspend fun addWatch(watch: Watch) {
        if (!discoveredWatches.contains(watch)) {
            discoveredWatches.add(watch)
            watchManager.registerWatch(watch)
        }
    }
}

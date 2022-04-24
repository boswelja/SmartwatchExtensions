package com.boswelja.smartwatchextensions.devicemanagement.ui.register

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A ViewModel for handling the discovery and registration of watches.
 */
class RegisterWatchViewModel(
    private val watchRepository: WatchRepository
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
            watchRepository.availableWatches.collect { watches ->
                watches
                    .filter {
                        watchRepository.getWatchById(it.uid).first() == null
                    }
                    .forEach { watch ->
                        addWatch(watch)
                    }
            }
        }
    }

    private suspend fun addWatch(watch: Watch) {
        if (!discoveredWatches.contains(watch)) {
            discoveredWatches.add(watch)
            watchRepository.registerWatch(watch)
        }
    }
}

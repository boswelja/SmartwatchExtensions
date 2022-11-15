package com.boswelja.smartwatchextensions.watchmanager.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.watchmanager.domain.AvailableWatch
import com.boswelja.smartwatchextensions.watchmanager.domain.AvailableWatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegisterWatchViewModel(
    private val watchRepository: WatchRepository,
    availableWatchRepository: AvailableWatchRepository
) : ViewModel() {

    val availableWatches = availableWatchRepository.getAvailableWatches()
        .map { availableWatches ->
            // Filter out all registered watches
            val registeredWatches = watchRepository.registeredWatches.first()
            availableWatches.filter { availableWatch ->
                registeredWatches.any { registeredWatch ->
                    registeredWatch.uid == availableWatch.id
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    fun registerWatch(watch: AvailableWatch) {
        viewModelScope.launch {
            watchRepository.registerWatch(Watch(watch.id, watch.name))
        }
    }
}

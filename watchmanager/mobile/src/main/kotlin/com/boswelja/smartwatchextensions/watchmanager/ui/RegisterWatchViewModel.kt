package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegisterWatchViewModel(
    private val watchRepository: WatchRepository
) : ViewModel() {

    val availableWatches = watchRepository.availableWatches
        .map { availableWatches ->
            // Filter out all registered watches
            val registeredWatches = watchRepository.registeredWatches.first()
            availableWatches.filter { availableWatch ->
                registeredWatches.any { registeredWatch ->
                    registeredWatch.uid == availableWatch.uid
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    fun registerWatch(watch: Watch) {
        viewModelScope.launch {
            watchRepository.registerWatch(watch)
        }
    }
}

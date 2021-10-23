package com.boswelja.smartwatchextensions.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val watchManager: WatchManager
) : ViewModel() {

    val selectedWatch = watchManager.selectedWatch
    val registeredWatches = watchManager.registeredWatches

    val needsSetup = registeredWatches.map { it.isEmpty() }

    fun selectWatchById(watchId: String) {
        viewModelScope.launch {
            watchManager.selectWatchById(watchId)
        }
    }
}

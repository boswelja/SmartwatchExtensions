package com.boswelja.smartwatchextensions.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to [MainActivity].
 */
class MainViewModel(
    private val watchManager: WatchManager
) : ViewModel() {

    /**
     * Flows the selected watch.
     */
    val selectedWatch = watchManager.selectedWatch

    /**
     * Flows the registered watches.
     */
    val registeredWatches = watchManager.registeredWatches

    /**
     * Flow whether the app needs setup.
     */
    val needsSetup = registeredWatches.map { it.isEmpty() }

    /**
     * Select a watch by it's ID.
     */
    fun selectWatchById(watchId: String) {
        viewModelScope.launch {
            watchManager.selectWatchById(watchId)
        }
    }
}

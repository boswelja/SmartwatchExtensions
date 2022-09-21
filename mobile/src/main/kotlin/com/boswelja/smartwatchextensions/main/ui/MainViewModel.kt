package com.boswelja.smartwatchextensions.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to [MainActivity].
 */
class MainViewModel(
    watchRepository: WatchRepository,
    private val selectedWatchManager: SelectedWatchManager
) : ViewModel() {

    /**
     * Flow whether the app needs setup.
     */
    val needsSetup = watchRepository.registeredWatches.map { it.isEmpty() }

    /**
     * Select a watch by it's ID.
     */
    fun selectWatchById(watchId: String) {
        viewModelScope.launch {
            selectedWatchManager.selectWatch(watchId)
        }
    }
}

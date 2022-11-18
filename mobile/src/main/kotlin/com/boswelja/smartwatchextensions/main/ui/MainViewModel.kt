package com.boswelja.smartwatchextensions.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchManager
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to [MainActivity].
 */
class MainViewModel(
    private val selectedWatchManager: SelectedWatchManager
) : ViewModel() {

    /**
     * Select a watch by it's ID.
     */
    fun selectWatchById(watchId: String) {
        viewModelScope.launch {
            selectedWatchManager.selectWatch(watchId)
        }
    }
}

package com.boswelja.smartwatchextensions.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to [MainActivity].
 */
class MainViewModel(
    private val selectedWatchController: SelectedWatchController
) : ViewModel() {

    /**
     * Select a watch by it's ID.
     */
    fun selectWatchById(watchId: String) {
        viewModelScope.launch {
            selectedWatchController.selectWatch(watchId)
        }
    }
}

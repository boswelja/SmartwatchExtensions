package com.boswelja.smartwatchextensions.watchmanager.ui.pick

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.devicemanagement.RegisteredWatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchPickerViewModel(
    registeredWatchRepository: RegisteredWatchRepository,
    private val selectedWatchManager: SelectedWatchManager
) : ViewModel() {

    val watches = registeredWatchRepository.registeredWatches
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    val selectedWatch = selectedWatchManager.selectedWatch
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun selectWatch(watch: Watch) {
        viewModelScope.launch {
            selectedWatchManager.selectWatch(watch)
        }
    }
}

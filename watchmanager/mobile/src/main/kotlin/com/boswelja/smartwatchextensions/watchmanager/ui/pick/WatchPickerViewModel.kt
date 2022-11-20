package com.boswelja.smartwatchextensions.watchmanager.ui.pick

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchPickerViewModel(
    registeredWatchRepository: RegisteredWatchRepository,
    private val selectedWatchController: SelectedWatchController
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val watches = registeredWatchRepository.registeredWatches
        .mapLatest { it.map { Watch(it.uid, it.name) } }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    val selectedWatch = selectedWatchController.selectedWatch
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun selectWatch(watch: Watch) {
        viewModelScope.launch {
            selectedWatchController.selectWatch(watch)
        }
    }
}

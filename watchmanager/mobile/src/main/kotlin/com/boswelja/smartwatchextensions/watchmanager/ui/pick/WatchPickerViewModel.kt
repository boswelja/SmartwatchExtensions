package com.boswelja.smartwatchextensions.watchmanager.ui.pick

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchPickerViewModel(
    private val registeredWatchRepository: RegisteredWatchRepository,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedWatch = selectedWatchController.selectedWatch
        .filterNotNull()
        .flatMapLatest { registeredWatchRepository.getWatchById(it) }
        .map { it?.let { Watch(it.uid, it.name) } }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun selectWatch(watch: Watch) {
        viewModelScope.launch {
            selectedWatchController.selectWatch(watch.uid)
        }
    }
}

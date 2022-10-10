package com.boswelja.smartwatchextensions.watchmanager.ui.manageregistered

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageRegisteredWatchViewModel(
    savedStateHandle: SavedStateHandle,
    private val watchRepository: WatchRepository
) : ViewModel() {

    private val watchId: String = checkNotNull(savedStateHandle["watchUid"])

    val watch = watchRepository.getWatchById(watchId)
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun renameWatch(newName: String) {
        viewModelScope.launch {
            watchRepository.renameWatch(watch.first()!!, newName)
        }
    }
}

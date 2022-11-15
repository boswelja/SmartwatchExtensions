package com.boswelja.smartwatchextensions.watchmanager.ui.manageregistered

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.RegisteredWatchRepository
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageRegisteredWatchViewModel(
    savedStateHandle: SavedStateHandle,
    private val registeredWatchRepository: RegisteredWatchRepository,
    private val watchSettingsRepository: WatchSettingsRepository
) : ViewModel() {

    private val watchId: String = checkNotNull(savedStateHandle["watchUid"])

    val watch = registeredWatchRepository.getWatchById(watchId)
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val watchStatus = watch
        .filterNotNull()
        .flatMapLatest { registeredWatchRepository.getStatusFor(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun renameWatch(newName: String) {
        viewModelScope.launch {
            registeredWatchRepository.renameWatch(watch.first()!!, newName)
        }
    }

    fun removeWatch() {
        viewModelScope.launch {
            registeredWatchRepository.deregisterWatch(watch.first()!!)
        }
    }

    fun resetWatchSettings() {
        viewModelScope.launch {
            watchSettingsRepository.deleteForWatch(watchId)
        }
    }
}

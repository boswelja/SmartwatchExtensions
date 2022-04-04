package com.boswelja.smartwatchextensions.devicemanagement.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to Watch Manager.
 */
class WatchManagerViewModel(
    private val watchRepository: WatchRepository
) : ViewModel() {

    /**
     * Flow the list of registered watches.
     */
    val registeredWatches = watchRepository.registeredWatches

    fun forgetWatch(watch: Watch) {
        viewModelScope.launch {
            watchRepository.deregisterWatch(watch)
        }
    }

    fun syncCapabilities(watch: Watch) {
        TODO()
    }

    fun renameWatch(watch: Watch, newName: String) {
        viewModelScope.launch {
            watchRepository.renameWatch(watch, newName)
        }
    }
}

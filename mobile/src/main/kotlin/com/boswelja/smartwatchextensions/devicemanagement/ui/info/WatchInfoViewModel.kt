package com.boswelja.smartwatchextensions.devicemanagement.ui.info

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to Watch Info.
 */
class WatchInfoViewModel(
    private val watchManager: WatchManager
) : ViewModel() {

    /**
     * A list of capabilities the watch has. Note you should call [getCapabilities] to refresh this
     * list.
     */
    val watchCapabilities = mutableStateListOf<String>()

    /**
     * Updates [Watch.name] in the database for the current watch.
     */
    suspend fun updateWatchName(watch: Watch, name: String) {
        watchManager.renameWatch(watch, name)
    }

    /**
     * Refresh capabilities for the given watch. Refreshed capabilities will be posted in
     * [watchCapabilities].
     */
    fun getCapabilities(watch: Watch) {
        viewModelScope.launch {
            watchManager.getCapabilitiesFor(watch).let {
                watchCapabilities.clear()
                watchCapabilities.addAll(it)
            }
        }
    }

    /**
     * Forgets the current watch.
     */
    fun forgetWatch(watch: Watch) {
        viewModelScope.launch {
            watchManager.forgetWatch(watch)
        }
    }

    /**
     * Resets the current watch preferences.
     */
    fun resetWatchPreferences(watch: Watch) {
        viewModelScope.launch {
            watchManager.resetWatchPreferences(watch)
        }
    }
}

package com.boswelja.smartwatchextensions.devicemanagement.ui.info

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.launch
import timber.log.Timber

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
    val watchCapabilities = mutableStateListOf<Capability>()

    /**
     * Updates [Watch.name] in the database for the current watch.
     */
    suspend fun updateWatchName(watch: Watch, name: String) {
        Timber.d("updateWatchName($name) called")
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
    suspend fun forgetWatch(watch: Watch) {
        watchManager.forgetWatch(watch)
    }

    /**
     * Resets the current watch preferences.
     */
    suspend fun resetWatchPreferences(watch: Watch) {
        watchManager.resetWatchPreferences(watch)
    }
}

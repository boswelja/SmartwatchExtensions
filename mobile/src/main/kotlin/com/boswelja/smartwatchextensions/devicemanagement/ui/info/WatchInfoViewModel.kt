package com.boswelja.smartwatchextensions.devicemanagement.ui.info

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchInfoViewModel(
    private val watchManager: WatchManager
) : ViewModel() {

    val watchCapabilities = mutableStateListOf<Capability>()

    /**
     * Updates [Watch.name] in the database for the current watch.
     */
    suspend fun updateWatchName(watch: Watch, name: String) {
        Timber.d("updateWatchName($name) called")
        watchManager.renameWatch(watch, name)
    }

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

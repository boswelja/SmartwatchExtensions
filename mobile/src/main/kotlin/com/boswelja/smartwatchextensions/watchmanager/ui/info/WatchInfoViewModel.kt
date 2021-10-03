package com.boswelja.smartwatchextensions.watchmanager.ui.info

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchInfoViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    val watchCapabilities = mutableStateListOf<Capability>()

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )

    /**
     * Updates [Watch.name] in the database for the current watch.
     */
    suspend fun updateWatchName(watch: Watch, name: String) {
        Timber.d("updateWatchName($name) called")
        watchManager.renameWatch(watch, name)
    }

    fun getCapabilities(watch: Watch) {
        viewModelScope.launch {
            watchManager.getCapabilitiesFor(watch)?.let {
                watchCapabilities.clear()
                watchCapabilities.addAll(it)
            }
        }
    }

    /**
     * Forgets the current watch.
     */
    suspend fun forgetWatch(watch: Watch) {
        watchManager.forgetWatch(getApplication(), watch)
    }

    /**
     * Resets the current watch preferences.
     */
    suspend fun resetWatchPreferences(watch: Watch) {
        watchManager.resetWatchPreferences(getApplication(), watch)
    }
}

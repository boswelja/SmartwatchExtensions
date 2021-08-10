package com.boswelja.smartwatchextensions.watchmanager.ui.info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber

class WatchInfoViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

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

    fun getCapabilities(watch: Watch) = watchManager.getCapabilitiesFor(watch)
        ?: flowOf(emptyList())

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

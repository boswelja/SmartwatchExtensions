package com.boswelja.smartwatchextensions.devicemanagement.ui.info

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber

class WatchInfoViewModel(
    application: Application
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val watchManager: WatchManager by instance()

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
        watchManager.forgetWatch(getApplication<Application>(), watch)
    }

    /**
     * Resets the current watch preferences.
     */
    suspend fun resetWatchPreferences(watch: Watch) {
        watchManager.resetWatchPreferences(getApplication<Application>(), watch)
    }
}

package com.boswelja.smartwatchextensions.devicemanagement.ui.register

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber

/**
 * A ViewModel for handling the discovery and registration of watches.
 */
class RegisterWatchViewModel(
    application: Application
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val watchManager: WatchManager by instance()

    var discoveredWatches = mutableStateListOf<Watch>()

    init {
        startRegisteringWatches()
    }

    fun startRegisteringWatches() {
        viewModelScope.launch {
            watchManager.availableWatches.collect { watches ->
                Timber.d("Got %s watches", watches.count())
                watches.forEach { watch ->
                    Timber.i("Adding watch %s", watch.uid)
                    addWatch(watch)
                }
            }
        }
    }

    suspend fun addWatch(watch: Watch) {
        Timber.d("registerWatch($watch) called")
        if (!discoveredWatches.contains(watch)) {
            discoveredWatches.add(watch)
            watchManager.registerWatch(watch)
        }
    }
}

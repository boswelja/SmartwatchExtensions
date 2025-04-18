package com.boswelja.smartwatchextensions.settings.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.platform.WatchBatteryTileService
import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchRepository
import com.boswelja.smartwatchextensions.core.settings.Settings
import com.boswelja.smartwatchextensions.core.watches.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to App Settings.
 */
class AppSettingsViewModel(
    application: Application,
    private val registeredWatchRepository: RegisteredWatchRepository,
    private val dataStore: DataStore<Settings>
) : AndroidViewModel(application) {

    /**
     * Flow the currently registered watches.
     */
    val registeredWatches = registeredWatchRepository.registeredWatches

    /**
     * Flow the currently selected watch for QS Tile data.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val qsTilesWatch = dataStore.data.map {
        it.qsTileWatchId
    }.flatMapLatest { idString ->
        if (idString.isNotEmpty()) {
            registeredWatchRepository.getWatchById(idString)
        } else {
            registeredWatchRepository.registeredWatches.map { it.firstOrNull() }
        }
    }

    /**
     * Set a watch for QS Tiles to display data for.
     */
    fun setQSTilesWatch(watch: Watch) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(qsTileWatchId = watch.uid)
            }

            WatchBatteryTileService.requestTileUpdate(getApplication())
        }
    }
}

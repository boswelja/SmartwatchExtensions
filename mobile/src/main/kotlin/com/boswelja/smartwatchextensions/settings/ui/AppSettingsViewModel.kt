package com.boswelja.smartwatchextensions.settings.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.batterysync.quicksettings.WatchBatteryTileService
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.settings.Settings
import com.boswelja.smartwatchextensions.updatechecker.UpdateCheckWorker
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to App Settings.
 */
class AppSettingsViewModel(
    application: Application,
    private val analytics: Analytics,
    private val watchManager: WatchManager,
    private val dataStore: DataStore<Settings>
) : AndroidViewModel(application) {

    /**
     * Flow whether analytics are enabled.
     */
    val analyticsEnabled = dataStore.data.map { it.analyticsEnabled }

    /**
     * Flow the currently registered watches.
     */
    val registeredWatches = watchManager.registeredWatches

    /**
     * Flow the currently selected watch for QS Tile data.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val qsTilesWatch = dataStore.data.map {
        it.qsTileWatchId
    }.flatMapLatest { idString ->
        if (idString.isNotEmpty()) {
            watchManager.getWatchById(idString)
        } else {
            watchManager.registeredWatches.map { it.firstOrNull() }
        }
    }

    /**
     * Flow whether daily update checks are enabled.
     */
    val checkUpdatesDaily = dataStore.data.map { it.checkForUpdates }

    /**
     * Set whether analytics are enabled.
     */
    fun setAnalyticsEnabled(analyticsEnabled: Boolean) {
        viewModelScope.launch {
            analytics.setAnalyticsEnabled(analyticsEnabled)
            dataStore.updateData {
                it.copy(analyticsEnabled = analyticsEnabled)
            }
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

    /**
     * Set whether daily update checking is enabled.
     */
    fun setCheckUpdatesDaily(newValue: Boolean) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(checkForUpdates = newValue)
            }
            if (newValue) {
                UpdateCheckWorker.schedule(getApplication())
            } else {
                UpdateCheckWorker.cancel(getApplication())
            }
        }
    }
}

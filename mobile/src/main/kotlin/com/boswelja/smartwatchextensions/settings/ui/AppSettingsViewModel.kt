package com.boswelja.smartwatchextensions.settings.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.batterysync.quicksettings.WatchBatteryTileService
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.settings.Settings
import com.boswelja.smartwatchextensions.settings.appSettingsStore
import com.boswelja.smartwatchextensions.updatechecker.UpdateCheckWorker
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class AppSettingsViewModel internal constructor(
    application: Application,
    private val dataStore: DataStore<Settings>,
    private val watchManager: WatchManager
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val analytics: Analytics by instance()

    val analyticsEnabled = dataStore.data.map { it.analyticsEnabled }
    val registeredWatches = watchManager.registeredWatches
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
    val checkUpdatesDaily = dataStore.data.map { it.checkForUpdates }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        application.appSettingsStore,
        WatchManager.getInstance(application)
    )

    fun setAnalyticsEnabled(analyticsEnabled: Boolean) {
        viewModelScope.launch {
            analytics.setAnalyticsEnabled(analyticsEnabled)
            dataStore.updateData {
                it.copy(analyticsEnabled = analyticsEnabled)
            }
        }
    }

    fun setQSTilesWatch(watch: Watch) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(qsTileWatchId = watch.uid)
            }

            WatchBatteryTileService.requestTileUpdate(getApplication())
        }
    }

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

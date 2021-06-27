package com.boswelja.smartwatchextensions.appsettings.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appsettings.Settings
import com.boswelja.smartwatchextensions.appsettings.appSettingsStore
import com.boswelja.smartwatchextensions.batterysync.quicksettings.WatchBatteryTileService
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppSettingsViewModel internal constructor(
    application: Application,
    private val dataStore: DataStore<Settings>,
    private val analytics: Analytics,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    val analyticsEnabled = dataStore.data.map { it.analyticsEnabled }
    val appTheme = dataStore.data.map { it.appTheme }
    val registeredWatches = watchManager.registeredWatches
    @ExperimentalCoroutinesApi
    val qsTilesWatch = dataStore.data.map {
        it.qsTileWatchId
    }.flatMapLatest { idString ->
        if (idString.isNotEmpty()) {
            watchManager.getWatchById(UUID.fromString(idString))
        } else {
            watchManager.registeredWatches.map { it.firstOrNull() }
        }
    }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        application.appSettingsStore,
        Analytics(),
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

    fun setAppTheme(appTheme: Settings.Theme) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(appTheme = appTheme)
            }
            AppCompatDelegate.setDefaultNightMode(
                when (appTheme) {
                    Settings.Theme.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    Settings.Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    Settings.Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                }
            )
        }
    }

    fun setQSTilesWatch(watch: Watch) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(qsTileWatchId = watch.id.toString())
            }

            WatchBatteryTileService.requestTileUpdate(getApplication())
        }
    }
}

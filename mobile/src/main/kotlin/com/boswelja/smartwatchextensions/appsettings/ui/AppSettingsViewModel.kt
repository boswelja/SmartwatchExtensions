package com.boswelja.smartwatchextensions.appsettings.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appsettings.Settings
import com.boswelja.smartwatchextensions.appsettings.appSettingsStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppSettingsViewModel internal constructor(
    application: Application,
    private val dataStore: DataStore<Settings>,
    private val analytics: Analytics
) : AndroidViewModel(application) {

    val analyticsEnabled = dataStore.data.map { it.analyticsEnabled }
    val appTheme = dataStore.data.map { it.appTheme }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        application.appSettingsStore,
        Analytics()
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
}

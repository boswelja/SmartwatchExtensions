package com.boswelja.smartwatchextensions.appsettings.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appsettings.Settings
import com.boswelja.smartwatchextensions.appsettings.appSettingsStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AppSettingsViewModel internal constructor(
    application: Application,
    private val dataStore: DataStore<Settings>,
    private val analytics: Analytics
) : AndroidViewModel(application) {

    private val _analyticsEnabled = MutableLiveData<Boolean>()
    private val _appTheme = MutableLiveData<Settings.Theme>()

    val analyticsEnabled: LiveData<Boolean>
        get() = _analyticsEnabled
    val appTheme: LiveData<Settings.Theme>
        get() = _appTheme

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        application.appSettingsStore,
        Analytics()
    )

    init {
        viewModelScope.launch {
            dataStore.data.collect {
                _analyticsEnabled.postValue(it.analyticsEnabled)
                _appTheme.postValue(it.appTheme)
            }
        }
    }

    fun setAnalyticsEnabled(analyticsEnabled: Boolean) {
        viewModelScope.launch {
            _analyticsEnabled.postValue(analyticsEnabled)
            analytics.setAnalyticsEnabled(analyticsEnabled)
            dataStore.updateData {
                it.copy(analyticsEnabled = analyticsEnabled)
            }
        }
    }

    fun setAppTheme(appTheme: Settings.Theme) {
        viewModelScope.launch {
            _appTheme.postValue(appTheme)
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

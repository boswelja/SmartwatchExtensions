package com.boswelja.devicemanager.appsettings.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.analytics.Analytics.Companion.ANALYTICS_ENABLED_KEY

class AppSettingsViewModel internal constructor(
    application: Application,
    private val sharedPreferences: SharedPreferences,
    private val analytics: Analytics
) : AndroidViewModel(application) {

    private val _analyticsEnabled = MutableLiveData(
        sharedPreferences.getBoolean(ANALYTICS_ENABLED_KEY, true)
    )
    private val _appTheme = MutableLiveData(
        sharedPreferences.getString(
            DAYNIGHT_MODE_KEY,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
        )?.toInt() ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    val analyticsEnabled: LiveData<Boolean>
        get() = _analyticsEnabled
    val appTheme: LiveData<Int>
        get() = _appTheme

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        PreferenceManager.getDefaultSharedPreferences(application),
        Analytics()
    )

    fun setAnalyticsEnabled(analyticsEnabled: Boolean) {
        analytics.setAnalyticsEnabled(analyticsEnabled)
        sharedPreferences.edit {
            putBoolean(ANALYTICS_ENABLED_KEY, analyticsEnabled)
        }
        _analyticsEnabled.postValue(analyticsEnabled)
    }

    fun setAppTheme(nightMode: Int) {
        AppCompatDelegate.setDefaultNightMode(nightMode)
        sharedPreferences.edit {
            putString(DAYNIGHT_MODE_KEY, nightMode.toString())
        }
        _appTheme.postValue(nightMode)
    }

    companion object {
        private const val DAYNIGHT_MODE_KEY = "daynight_mode"
    }
}

package com.boswelja.devicemanager.widget.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget.Companion.WIDGET_BACKGROUND_OPACITY_KEY

class WidgetSettingsViewModel internal constructor(
    application: Application,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        PreferenceManager.getDefaultSharedPreferences(application)
    )

    private val _widgetBackgroundVisible =
        MutableLiveData(sharedPreferences.getBoolean(SHOW_WIDGET_BACKGROUND_KEY, true))
    private val _widgetBackgroundOpacity =
        MutableLiveData(sharedPreferences.getInt(WIDGET_BACKGROUND_OPACITY_KEY, 60))

    val widgetBackgroundVisible: LiveData<Boolean>
        get() = _widgetBackgroundVisible
    val widgetBackgroundOpacity: LiveData<Int>
        get() = _widgetBackgroundOpacity

    fun setShowBackground(showBackground: Boolean) {
        _widgetBackgroundVisible.postValue(showBackground)
        sharedPreferences.edit {
            putBoolean(SHOW_WIDGET_BACKGROUND_KEY, showBackground)
        }
    }

    fun setBackgroundOpacity(opacity: Int) {
        _widgetBackgroundOpacity.postValue(opacity)
        sharedPreferences.edit {
            putInt(WIDGET_BACKGROUND_OPACITY_KEY, opacity)
        }
    }
}

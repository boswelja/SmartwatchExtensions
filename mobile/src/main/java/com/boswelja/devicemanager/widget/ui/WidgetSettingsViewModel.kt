/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.appsettings.ui.AppSettingsFragment.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.devicemanager.appsettings.ui.AppSettingsFragment.Companion.WIDGET_BACKGROUND_OPACITY_KEY

class WidgetSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            WIDGET_BACKGROUND_OPACITY_KEY ->
                _widgetBackgroundOpacity.postValue(sharedPreferences.getInt(key, 60))
            SHOW_WIDGET_BACKGROUND_KEY ->
                _widgetBackgroundVisible.postValue(sharedPreferences.getBoolean(key, true))
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private val _widgetBackgroundVisible = MutableLiveData(sharedPreferences.getBoolean(SHOW_WIDGET_BACKGROUND_KEY, true))
    val widgetBackgroundVisible: LiveData<Boolean>
        get() = _widgetBackgroundVisible

    private val _widgetBackgroundOpacity = MutableLiveData(sharedPreferences.getInt(WIDGET_BACKGROUND_OPACITY_KEY, 60))
    val widgetBackgroundOpacity: LiveData<Int>
        get() = _widgetBackgroundOpacity
}

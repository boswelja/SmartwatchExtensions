package com.boswelja.smartwatchextensions.widget.ui

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.widget.ui.WidgetSettingsActivity.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.smartwatchextensions.widget.ui.WidgetSettingsActivity.Companion.WIDGET_BACKGROUND_OPACITY_KEY
import com.boswelja.smartwatchextensions.widget.widgetSettings
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WidgetSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val widgetSettings = application.widgetSettings

    private val _widgetBackgroundVisible = MutableLiveData(true)
    private val _widgetBackgroundOpacity = MutableLiveData(60)

    val widgetBackgroundVisible: LiveData<Boolean>
        get() = _widgetBackgroundVisible
    val widgetBackgroundOpacity: LiveData<Int>
        get() = _widgetBackgroundOpacity

    init {
        viewModelScope.launch {
            widgetSettings.data.map { it[SHOW_WIDGET_BACKGROUND_KEY] }.collect {
                _widgetBackgroundVisible.postValue(it ?: true)
            }
            widgetSettings.data.map { it[WIDGET_BACKGROUND_OPACITY_KEY] }.collect {
                _widgetBackgroundOpacity.postValue(it ?: 60)
            }
        }
    }

    fun setShowBackground(showBackground: Boolean) {
        _widgetBackgroundVisible.postValue(showBackground)
        viewModelScope.launch {
            widgetSettings.edit {
                it[SHOW_WIDGET_BACKGROUND_KEY] = showBackground
            }
        }
    }

    fun setBackgroundOpacity(opacity: Int) {
        _widgetBackgroundOpacity.postValue(opacity)
        viewModelScope.launch {
            widgetSettings.edit {
                it[WIDGET_BACKGROUND_OPACITY_KEY] = opacity
            }
        }
    }
}

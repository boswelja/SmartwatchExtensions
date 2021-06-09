package com.boswelja.smartwatchextensions.widget.ui

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.widget.ui.WidgetSettingsActivity.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.smartwatchextensions.widget.ui.WidgetSettingsActivity.Companion.WIDGET_BACKGROUND_OPACITY_KEY
import com.boswelja.smartwatchextensions.widget.widgetSettings
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WidgetSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val widgetSettings = application.widgetSettings

    val widgetBackgroundVisible = widgetSettings.data.map { it[SHOW_WIDGET_BACKGROUND_KEY] }
    val widgetBackgroundOpacity = widgetSettings.data.map { it[WIDGET_BACKGROUND_OPACITY_KEY] }

    fun setShowBackground(showBackground: Boolean) {
        viewModelScope.launch {
            widgetSettings.edit {
                it[SHOW_WIDGET_BACKGROUND_KEY] = showBackground
            }
        }
    }

    fun setBackgroundOpacity(opacity: Int) {
        viewModelScope.launch {
            widgetSettings.edit {
                it[WIDGET_BACKGROUND_OPACITY_KEY] = opacity
            }
        }
    }
}

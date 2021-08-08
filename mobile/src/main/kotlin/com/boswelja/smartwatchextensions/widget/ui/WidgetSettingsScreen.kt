package com.boswelja.smartwatchextensions.widget.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.CheckboxSetting
import com.boswelja.smartwatchextensions.common.ui.HeaderItem
import com.boswelja.smartwatchextensions.common.ui.SliderSetting
import kotlinx.coroutines.Dispatchers

@Composable
fun WidgetSettingsScreen() {
    val viewModel: WidgetSettingsViewModel = viewModel()

    val backgroundVisible by viewModel.widgetBackgroundVisible
        .collectAsState(true, Dispatchers.IO)
    val backgroundOpacity by viewModel.widgetBackgroundOpacity
        .collectAsState(60, Dispatchers.IO)
    var currentOpacity by remember {
        mutableStateOf((backgroundOpacity ?: 60) / 100f)
    }
    Column {
        HeaderItem(
            text = { Text(stringResource(R.string.pref_category_widget_customisation)) }
        )
        CheckboxSetting(
            label = { Text(stringResource(R.string.pref_show_widget_background_title)) },
            checked = backgroundVisible == true,
            onCheckChanged = {
                viewModel.setShowBackground(it)
            }
        )
        SliderSetting(
            label = { Text(stringResource(R.string.pref_widget_background_opacity_title)) },
            value = currentOpacity,
            onSliderValueChanged = {
                currentOpacity = it
            },
            trailingFormat = "%s%%",
            onSliderValueFinished = {
                viewModel.setBackgroundOpacity((currentOpacity * 100).toInt())
            }
        )
    }
}

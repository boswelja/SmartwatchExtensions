package com.boswelja.smartwatchextensions.widget.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.CheckboxPreference
import com.boswelja.smartwatchextensions.common.ui.HeaderItem
import com.boswelja.smartwatchextensions.common.ui.SliderPreference
import kotlinx.coroutines.Dispatchers

@ExperimentalMaterialApi
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
        HeaderItem(stringResource(R.string.pref_category_widget_customisation))
        CheckboxPreference(
            text = stringResource(R.string.pref_show_widget_background_title),
            isChecked = backgroundVisible == true,
            onCheckChanged = {
                viewModel.setShowBackground(it)
            }
        )
        SliderPreference(
            text = stringResource(R.string.pref_widget_background_opacity_title),
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

package com.boswelja.devicemanager.widget.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.ui.HeaderItem
import com.boswelja.devicemanager.common.ui.CheckboxPreference
import com.boswelja.devicemanager.common.ui.SliderPreference

@ExperimentalMaterialApi
@Composable
fun WidgetSettingsScreen() {
    val viewModel: WidgetSettingsViewModel = viewModel()

    val showBackground by viewModel.widgetBackgroundVisible.observeAsState()
    val backgroundOpacity by viewModel.widgetBackgroundOpacity.observeAsState()
    var currentOpacity by remember {
        mutableStateOf((backgroundOpacity ?: 60) / 100f)
    }
    Column {
        HeaderItem(stringResource(R.string.pref_category_widget_customisation))
        CheckboxPreference(
            text = stringResource(R.string.pref_show_widget_background_title),
            isChecked = showBackground == true,
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

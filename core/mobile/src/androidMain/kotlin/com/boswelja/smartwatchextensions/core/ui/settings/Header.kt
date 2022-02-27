package com.boswelja.smartwatchextensions.core.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Settings header is used to separate groups of settings in the UI. This should only contain some text.
 */
@Composable
fun SettingsHeader(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(SettingDefaults.Padding),
        contentAlignment = Alignment.CenterStart
    ) {
        ProvideTextStyle(SettingDefaults.HeaderTextStyle) {
            text()
        }
    }
}

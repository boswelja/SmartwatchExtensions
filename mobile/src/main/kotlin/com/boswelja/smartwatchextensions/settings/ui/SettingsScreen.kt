package com.boswelja.smartwatchextensions.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppSettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onNavigateTo: (SettingsDestination) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        item {
            AppSettingsCard()
        }
        item {
            QSTileSettingsCard()
        }
        item {
            WatchSettings(onNavigateTo = onNavigateTo)
        }
    }
}

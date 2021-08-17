package com.boswelja.smartwatchextensions.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WatchSettings(
    modifier: Modifier = Modifier,
    onNavigateTo: (SettingsDestination) -> Unit
) {
    Card(
        modifier = modifier,
        header = {
            CardHeader(title = { Text(stringResource(R.string.category_watch_settings)) })
        }
    ) {
        Column {
            ListItem(
                text = { Text(stringResource(R.string.manage_watches_title)) },
                icon = { Icon(Icons.Outlined.Watch, null) },
                modifier = Modifier.clickable { onNavigateTo(SettingsDestination.WATCH_MANAGER) }
            )
        }
    }
}

package com.boswelja.smartwatchextensions.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader

/**
 * A Composable for displaying watch settings.
 * @param modifier [Modifier].
 * @param onNavigateTo Called when navigation is requested.
 */
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
        Row(
            modifier = Modifier.clickable { onNavigateTo(SettingsDestination.WATCH_MANAGER) }
        ) {
            Icon(Icons.Outlined.Watch, null)
            Text(stringResource(R.string.manage_watches_title))
        }
    }
}

package com.boswelja.smartwatchextensions.appsettings.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.watchmanager.ui.WatchManagerActivity

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WatchSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    SettingsCard(
        modifier = modifier,
        title = { Text(stringResource(R.string.category_watch_settings)) }
    ) {
        Column {
            ListItem(
                text = { Text(stringResource(R.string.manage_watches_title)) },
                icon = { Icon(Icons.Outlined.Watch, null) },
                modifier = Modifier.clickable {
                    context.startActivity<WatchManagerActivity>()
                }
            )
        }
    }
}

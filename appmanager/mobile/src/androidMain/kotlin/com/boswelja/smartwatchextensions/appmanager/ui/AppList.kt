package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.appmanager.R
import com.boswelja.smartwatchextensions.appmanager.WatchApp

/**
 * A Composable for displaying a list of apps.
 * @param modifier [Modifier].
 * @param contentPadding The content padding.
 * @param userApps The list of user apps.
 * @param disabledApps The list of disabled apps.
 * @param systemApps The list of system apps.
 * @param onAppClick Called when an app is clicked.
 */
@Composable
fun AppList(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    userApps: List<WatchApp>,
    disabledApps: List<WatchApp>,
    systemApps: List<WatchApp>,
    onAppClick: (WatchApp) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        item {
            Text(stringResource(R.string.appmanager_section_user_apps))
        }
        if (userApps.isNotEmpty()) {
            items(
                count = systemApps.size,
                key = { systemApps[it].packageName }
            ) {
                AppItem(app = systemApps[it], onClick = onAppClick)
            }
        }
        item {
            Text(stringResource(R.string.appmanager_section_disabled_apps))
        }
        if (disabledApps.isNotEmpty()) {
            items(
                count = disabledApps.size,
                key = { disabledApps[it].packageName }
            ) {
                AppItem(app = disabledApps[it], onClick = onAppClick)
            }
        }
        item {
            Text(stringResource(R.string.appmanager_section_system_apps))
        }
        if (systemApps.isNotEmpty()) {
            items(
                count = systemApps.size,
                key = { systemApps[it].packageName }
            ) {
                AppItem(app = systemApps[it], onClick = onAppClick)
            }
        }
    }
}

/**
 * A Composable for displaying an app.
 * @param modifier [Modifier].
 * @param app The app to display.
 * @param onClick Called when the app is clicked.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppItem(
    modifier: Modifier = Modifier,
    app: WatchApp,
    onClick: (WatchApp) -> Unit
) {
    ListItem(
        text = { Text(app.label) },
        secondaryText = { Text(app.versionName) },
        icon = {
            Image(
                Icons.Outlined.Info,
                contentDescription = null,
                Modifier.size(40.dp)
            )
        },
        modifier = modifier.clickable { onClick(app) }
    )
}

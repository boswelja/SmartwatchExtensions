package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * @param userApps The list of user apps.
 * @param disabledApps The list of disabled apps.
 * @param systemApps The list of system apps.
 * @param onAppClick Called when an app is clicked.
 * @param modifier [Modifier].
 * @param contentPadding The content padding.
 */
@Composable
fun AppList(
    userApps: List<WatchApp>,
    disabledApps: List<WatchApp>,
    systemApps: List<WatchApp>,
    onAppClick: (WatchApp) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        item {
            Text(stringResource(R.string.appmanager_section_user))
        }
        if (userApps.isNotEmpty()) {
            items(
                items = userApps,
                key = { it.packageName }
            ) { app ->
                AppItem(app = app, onClick = onAppClick)
            }
        }
        item {
            Text(stringResource(R.string.appmanager_section_disabled))
        }
        if (disabledApps.isNotEmpty()) {
            items(
                items = disabledApps,
                key = { it.packageName }
            ) { app ->
                AppItem(app = app, onClick = onAppClick)
            }
        }
        item {
            Text(stringResource(R.string.appmanager_section_system))
        }
        if (systemApps.isNotEmpty()) {
            items(
                items = systemApps,
                key = { it.packageName }
            ) { app ->
                AppItem(app = app, onClick = onAppClick)
            }
        }
    }
}

/**
 * A Composable for displaying an app.
 * @param app The app to display.
 * @param onClick Called when the app is clicked.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppItem(
    app: WatchApp,
    onClick: (WatchApp) -> Unit,
    modifier: Modifier = Modifier
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

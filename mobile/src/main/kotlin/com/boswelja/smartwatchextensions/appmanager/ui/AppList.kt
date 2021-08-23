package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.database.DbApp
import com.boswelja.smartwatchextensions.common.ui.ExpandableCard

@Composable
fun AppList(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    userApps: List<DbApp>,
    disabledApps: List<DbApp>,
    systemApps: List<DbApp>,
    onAppClick: (DbApp) -> Unit
) {
    var userAppsExpanded by rememberSaveable { mutableStateOf(true) }
    var disabledAppsExpanded by rememberSaveable { mutableStateOf(true) }
    var systemAppsExpanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        if (userApps.isNotEmpty()) {
            item {
                ExpandableCard(
                    title = { Text(stringResource(R.string.app_manager_section_user_apps)) },
                    expanded = userAppsExpanded,
                    toggleExpanded = { userAppsExpanded = !userAppsExpanded }
                ) {
                    Column {
                        userApps.forEach { app ->
                            AppItem(
                                app = app,
                                onClick = onAppClick
                            )
                        }
                    }
                }
            }
        }
        if (disabledApps.isNotEmpty()) {
            item {
                ExpandableCard(
                    title = { Text(stringResource(R.string.app_manager_section_disabled_apps)) },
                    expanded = disabledAppsExpanded,
                    toggleExpanded = { disabledAppsExpanded = !disabledAppsExpanded }
                ) {
                    Column {
                        disabledApps.forEach { app ->
                            AppItem(
                                app = app,
                                onClick = onAppClick
                            )
                        }
                    }
                }
            }
        }
        if (systemApps.isNotEmpty()) {
            item {
                ExpandableCard(
                    title = { Text(stringResource(R.string.app_manager_section_system_apps)) },
                    expanded = systemAppsExpanded,
                    toggleExpanded = { systemAppsExpanded = !systemAppsExpanded }
                ) {
                    Column {
                        systemApps.forEach { app ->
                            AppItem(
                                app = app,
                                onClick = onAppClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppItem(
    modifier: Modifier = Modifier,
    app: DbApp,
    onClick: (DbApp) -> Unit
) {
    ListItem(
        text = { Text(app.label) },
        secondaryText = { Text(app.version) },
        icon = {
            if (app.icon != null) {
                Image(
                    app.icon.asImageBitmap(),
                    contentDescription = null,
                    Modifier.size(40.dp)
                )
            } else {
                Image(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    Modifier.size(40.dp)
                )
            }
        },
        modifier = modifier.clickable { onClick(app) }
    )
}

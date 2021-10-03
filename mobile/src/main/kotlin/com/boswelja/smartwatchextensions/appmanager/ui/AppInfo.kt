package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.database.DbApp
import com.boswelja.smartwatchextensions.common.ui.BigButton
import com.boswelja.smartwatchextensions.common.ui.ExpandableCard
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AppInfo(
    modifier: Modifier = Modifier,
    app: DbApp,
    interactionEnabled: Boolean = true,
    onOpenClicked: (DbApp) -> Unit,
    onUninstallClicked: (DbApp) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppHeaderView(
            modifier = Modifier.fillMaxWidth(),
            appIcon = null,
            appName = app.label
        )
        AppActionButtons(
            modifier = Modifier.fillMaxWidth(),
            openEnabled = interactionEnabled && app.hasLaunchActivity,
            uninstallEnabled = interactionEnabled && !app.isSystemApp,
            onOpenClicked = { onOpenClicked(app) },
            onUninstallClicked = { onUninstallClicked(app) }
        )
        PermissionsInfo(
            modifier = Modifier.fillMaxWidth(),
            permissions = app.requestedPermissions
        )
        AppInstallInfo(
            modifier = Modifier.fillMaxWidth(),
            app = app
        )
    }
}

@Composable
fun AppHeaderView(
    modifier: Modifier = Modifier,
    appIcon: ImageBitmap?,
    appName: String
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (appIcon != null) {
            Image(
                appIcon,
                null,
                Modifier.size(72.dp)
            )
        } else {
            Image(
                Icons.Outlined.Info,
                null,
                Modifier.size(72.dp)
            )
        }
        Text(
            appName,
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun AppActionButtons(
    modifier: Modifier = Modifier,
    openEnabled: Boolean = true,
    uninstallEnabled: Boolean = true,
    onOpenClicked: () -> Unit,
    onUninstallClicked: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            BigButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Outlined.OpenInNew, null) },
                text = { Text(stringResource(R.string.app_info_open_button)) },
                onClick = onOpenClicked,
                enabled = openEnabled
            )
            BigButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Outlined.Delete, null) },
                text = { Text(stringResource(R.string.app_info_uninstall_button)) },
                onClick = onUninstallClicked,
                enabled = uninstallEnabled
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PermissionsInfo(
    modifier: Modifier = Modifier,
    permissions: List<String>
) {
    if (permissions.isNotEmpty()) {
        var isExpanded by remember { mutableStateOf(false) }
        ExpandableCard(
            modifier = modifier,
            title = {
                val permissionText = LocalContext.current.resources.getQuantityString(
                    R.plurals.app_info_requested_permissions_count,
                    permissions.count(),
                    permissions.count()
                )
                Text(permissionText)
            },
            expanded = isExpanded,
            toggleExpanded = { isExpanded = !isExpanded }
        ) {
            Column {
                permissions.forEach { permission ->
                    ListItem(
                        text = { Text(permission) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppInstallInfo(
    modifier: Modifier = Modifier,
    app: DbApp
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, h:mm aa", Locale.getDefault())
    }
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!app.isSystemApp) {
            Text(
                stringResource(
                    R.string.app_info_first_installed_prefix,
                    dateFormatter.format(app.installTime)
                ),
                style = MaterialTheme.typography.body2
            )
        }
        if (app.installTime < app.lastUpdateTime) {
            Text(
                stringResource(
                    R.string.app_info_last_updated_prefix,
                    dateFormatter.format(app.lastUpdateTime)
                ),
                style = MaterialTheme.typography.body2
            )
        }
        Text(
            stringResource(R.string.app_info_version_prefix, app.version),
            style = MaterialTheme.typography.body2
        )
    }
}

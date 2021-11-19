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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.appmanager.R
import com.boswelja.smartwatchextensions.appmanager.WatchAppDetails
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays detailed information about an app.
 */
@Composable
fun AppInfo(
    app: WatchAppDetails,
    onOpenClicked: (WatchAppDetails) -> Unit,
    onUninstallClicked: (WatchAppDetails) -> Unit,
    modifier: Modifier = Modifier,
    interactionEnabled: Boolean = true
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
            openEnabled = interactionEnabled && app.isLaunchable,
            uninstallEnabled = interactionEnabled && !app.isSystemApp,
            onOpenClicked = { onOpenClicked(app) },
            onUninstallClicked = { onUninstallClicked(app) }
        )
        PermissionsInfo(
            modifier = Modifier.fillMaxWidth(),
            permissions = app.permissions
        )
        AppInstallInfo(
            modifier = Modifier.fillMaxWidth(),
            app = app
        )
    }
}

/**
 * Displays a header for an app, including app icon and name.
 */
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

/**
 * Displays actions the user can take for an app.
 */
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
                text = { Text(stringResource(R.string.appinfo_open)) },
                onClick = onOpenClicked,
                enabled = openEnabled
            )
            BigButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Outlined.Delete, null) },
                text = { Text(stringResource(R.string.appinfo_uninstall)) },
                onClick = onUninstallClicked,
                enabled = uninstallEnabled
            )
        }
    }
}

/**
 * Displays information about an app's permissions.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PermissionsInfo(
    permissions: List<String>,
    modifier: Modifier = Modifier
) {
    if (permissions.isNotEmpty()) {
        val context = LocalContext.current
        val permissionText = remember(context) {
            context.resources.getQuantityString(
                R.plurals.appinfo_requested_permissions_count,
                permissions.count(),
                permissions.count()
            )
        }
        ListItem(
            modifier = modifier,
            text = { Text(permissionText) }
        )
        permissions.forEach { permission ->
            ListItem(
                text = { Text(permission) }
            )
        }
    }
}

/**
 * Display app install time information.
 */
@Composable
fun AppInstallInfo(
    modifier: Modifier = Modifier,
    app: WatchAppDetails
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
                    R.string.appinfo_first_installed,
                    dateFormatter.format(app.installTime)
                ),
                style = MaterialTheme.typography.body2
            )
        }
        if (app.installTime < app.updateTime) {
            Text(
                stringResource(
                    R.string.appinfo_last_updated,
                    dateFormatter.format(app.updateTime)
                ),
                style = MaterialTheme.typography.body2
            )
        }
        Text(
            stringResource(R.string.appinfo_version, app.versionName),
            style = MaterialTheme.typography.body2
        )
    }
}

package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.appmanager.R
import com.boswelja.smartwatchextensions.appmanager.WatchAppDetailsWithIcon
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays detailed information about an app.
 */
@Composable
fun AppInfo(
    app: WatchAppDetailsWithIcon,
    onOpenClicked: (WatchAppDetailsWithIcon) -> Unit,
    onUninstallClicked: (WatchAppDetailsWithIcon) -> Unit,
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
            appIcon = app.icon?.asImageBitmap(),
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
            style = MaterialTheme.typography.titleLarge
        )
    }
}

/**
 * Displays actions the user can take for an app.
 */
@Composable
fun AppActionButtons(
    onOpenClicked: () -> Unit,
    onUninstallClicked: () -> Unit,
    openEnabled: Boolean,
    uninstallEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        FilledTonalButton(
            onClick = onOpenClicked,
            enabled = openEnabled
        ) {
            Icon(Icons.Default.OpenInNew, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.appinfo_open))
        }
        FilledTonalButton(
            onClick = onUninstallClicked,
            enabled = uninstallEnabled
        ) {
            Icon(Icons.Default.Delete, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.appinfo_uninstall))
        }
    }
}

/**
 * Displays information about an app's permissions.
 */
@Composable
fun PermissionsInfo(
    permissions: List<String>,
    modifier: Modifier = Modifier
) {
    if (permissions.isNotEmpty()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val context = LocalContext.current
            val permissionText = remember(context) {
                context.resources.getQuantityString(
                    R.plurals.appinfo_requested_permissions_count,
                    permissions.count(),
                    permissions.count()
                )
            }
            Text(permissionText)
            permissions.forEach { permission ->
                Text(permission)
            }
        }
    }
}

/**
 * Display app install time information.
 */
@Composable
fun AppInstallInfo(
    modifier: Modifier = Modifier,
    app: WatchAppDetailsWithIcon
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
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (app.installTime < app.updateTime) {
            Text(
                stringResource(
                    R.string.appinfo_last_updated,
                    dateFormatter.format(app.updateTime)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            stringResource(R.string.appinfo_version, app.versionName),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

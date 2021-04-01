package com.boswelja.devicemanager.appmanager.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.App
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun AppInfo(
    app: App?,
    scaffoldState: ScaffoldState,
    viewModel: AppManagerViewModel
) {
    app?.let {
        val scrollState = rememberScrollState()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            AppHeaderView(app = it)
            ActionButtons(app = it, scaffoldState = scaffoldState, viewModel)
            PermissionsInfo(it.requestedPermissions)
            AppInstallInfo(it, viewModel)
        }
    }
}

@Composable
fun AppHeaderView(app: App) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (app.icon?.bitmap != null) {
            Image(
                app.icon!!.bitmap.asImageBitmap(),
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
            app.label,
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun ActionButtons(app: App, scaffoldState: ScaffoldState, viewModel: AppManagerViewModel) {
    val scope = rememberCoroutineScope()
    val continueOnWatchText = stringResource(R.string.watch_manager_action_continue_on_watch)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedButton(
            onClick = {
                viewModel.sendOpenRequest(app)
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(continueOnWatchText, null)
                }
            },
            Modifier.weight(1f)
        ) {
            Icon(Icons.Outlined.OpenInNew, null)
            Text(stringResource(R.string.app_info_open_button))
        }
        OutlinedButton(
            onClick = {
                viewModel.sendUninstallRequest(app)
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(continueOnWatchText, null)
                }
            },
            Modifier.weight(1f)
        ) {
            Icon(Icons.Outlined.Delete, null)
            Text(stringResource(R.string.app_info_uninstall_button))
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun PermissionsInfo(permissions: Array<String>) {
    if (permissions.isNotEmpty()) {
        var isExpanded by remember { mutableStateOf(false) }
        Column(
            Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .animateContentSize(tween(easing = FastOutSlowInEasing))
        ) {
            val permissionText = LocalContext.current.resources.getQuantityString(
                R.plurals.app_info_requested_permissions_count,
                permissions.count(),
                permissions.count()
            )
            ListItem(
                text = { Text(permissionText) },
                secondaryText = { Text("Tap to show more") },
                trailing = {
                    Icon(
                        if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        null
                    )
                }
            )
            if (isExpanded) {
                Column(Modifier.padding(16.dp)) {
                    permissions.forEach { permission ->
                        ListItem(
                            text = { Text(permission) }
                        )
                    }
                }
            }
        }
    } else {
        ListItem(
            text = { Text(stringResource(R.string.app_info_requested_permissions_none)) }
        )
    }
}

@Composable
fun AppInstallInfo(app: App, viewModel: AppManagerViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(
                R.string.app_info_first_installed_prefix,
                viewModel.formatDate(app.installTime)
            ),
            style = MaterialTheme.typography.body2
        )
        if (app.installTime < app.lastUpdateTime) {
            Text(
                stringResource(
                    R.string.app_info_last_updated_prefix,
                    viewModel.formatDate(app.lastUpdateTime)
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

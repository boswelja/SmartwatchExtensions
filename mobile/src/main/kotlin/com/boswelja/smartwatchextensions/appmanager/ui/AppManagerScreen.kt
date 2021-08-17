package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.common.ui.Banner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppManagerScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onShowSnackbar: suspend (String) -> Unit
) {
    val viewModel: AppManagerViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val userApps by viewModel.userApps.collectAsState(emptyList(), Dispatchers.IO)
    val disabledApps by viewModel.disabledApps.collectAsState(emptyList(), Dispatchers.IO)
    val systemApps by viewModel.systemApps.collectAsState(emptyList(), Dispatchers.IO)
    val isWatchConnected by viewModel.isWatchConnected.collectAsState(true, Dispatchers.IO)
    // Only check system apps (for now). It's effectively guaranteed we'll have some on any device
    val isLoading = systemApps.isEmpty() || viewModel.isUpdatingCache

    var selectedApp by remember { mutableStateOf<App?>(null) }
    var watchConnectionWarningVisible by remember(isLoading, isWatchConnected) {
        mutableStateOf(!isLoading && !isWatchConnected)
    }

    BackHandler(enabled = selectedApp != null) {
        selectedApp = null
    }

    Crossfade(targetState = selectedApp) {
        if (it != null) {
            AppInfo(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .padding(16.dp),
                app = it,
                onOpenClicked = {
                    coroutineScope.launch {
                        if (viewModel.sendOpenRequest(it)) {
                            onShowSnackbar(
                                context.getString(R.string.watch_manager_action_continue_on_watch)
                            )
                        }
                    }
                },
                onUninstallClicked = {
                    coroutineScope.launch {
                        if (viewModel.sendUninstallRequest(it)) {
                            selectedApp = null
                            onShowSnackbar(
                                context.getString(R.string.watch_manager_action_continue_on_watch)
                            )
                        }
                    }
                }
            )
        } else {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoadingIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = isLoading
                )
                WatchStatusIndicator(
                    visible = watchConnectionWarningVisible,
                    onDismissRequest = { watchConnectionWarningVisible = false }
                )
                AppList(
                    contentPadding = contentPadding,
                    userApps = userApps,
                    disabledApps = disabledApps,
                    systemApps = systemApps,
                    onAppClick = { app -> selectedApp = app }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    isLoading: Boolean
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        LinearProgressIndicator(modifier)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WatchStatusIndicator(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismissRequest: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column {
            Banner(
                modifier = modifier,
                icon = {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.app_manager_watch_disconnected)) },
                primaryButton = {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.watch_connection_dismiss))
                    }
                }
            )
            Divider()
        }
    }
}

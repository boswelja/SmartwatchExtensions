package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.WatchAppDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A Composable screen to display App Manager.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onShowSnackbar Called when a snackbar should be displayed.
 */
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
    val isUpdatingCache by viewModel.isUpdatingCache.collectAsState()
    // Only check system apps (for now). It's effectively guaranteed we'll have some on any device
    val isLoading = systemApps.isEmpty() || isUpdatingCache

    var selectedApp by remember { mutableStateOf<WatchAppDetails?>(null) }

    BackHandler(enabled = selectedApp != null) { selectedApp = null }

    Crossfade(targetState = selectedApp) {
        if (it != null) {
            AppInfo(
                modifier = Modifier
                    .fillMaxSize()
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
                AppList(
                    contentPadding = contentPadding,
                    userApps = userApps,
                    disabledApps = disabledApps,
                    systemApps = systemApps,
                    onAppClick = { app ->
                        coroutineScope.launch { selectedApp = viewModel.getDetailsFor(app) }
                    }
                )
            }
        }
    }
}

/**
 * A Composable for displaying a horizontal loading indicator.
 * @param modifier [Modifier].
 * @param isLoading Whether the loading indicator is visible.
 */
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

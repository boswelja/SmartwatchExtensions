package com.boswelja.smartwatchextensions.appmanager.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.Banner
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class AppManagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val continueOnWatchText =
                stringResource(R.string.watch_manager_action_continue_on_watch)
            val viewModel: AppManagerViewModel = viewModel()

            val registeredWatches by viewModel.registeredWatches
                .collectAsState(emptyList(), Dispatchers.IO)

            val scaffoldState = rememberScaffoldState()

            AppTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationWatchPickerAppBar(
                            selectedWatch = viewModel.selectedWatch,
                            watches = registeredWatches,
                            onWatchSelected = { viewModel.selectWatchById(it.id) },
                            onNavigateUp = { onBackPressed() }
                        )
                    }
                ) {
                    AppManagerScreen(
                        onOpenClicked = {
                            coroutineScope.launch {
                                if (viewModel.sendOpenRequest(it)) {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        continueOnWatchText, null
                                    )
                                }
                            }
                        },
                        onUninstallClicked = {
                            coroutineScope.launch {
                                if (viewModel.sendUninstallRequest(it)) {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        continueOnWatchText, null
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppManagerScreen(
    onOpenClicked: (App) -> Unit,
    onUninstallClicked: (App) -> Unit
) {
    val viewModel: AppManagerViewModel = viewModel()

    val userApps by viewModel.userApps.collectAsState(emptyList(), Dispatchers.IO)
    val disabledApps by viewModel.disabledApps.collectAsState(emptyList(), Dispatchers.IO)
    val systemApps by viewModel.systemApps.collectAsState(emptyList(), Dispatchers.IO)
    val isWatchConnected by viewModel.isWatchConnected.collectAsState(true, Dispatchers.IO)
    // Only check system apps (for now). It's effectively guaranteed we'll have some on any device
    val isLoading = systemApps.isEmpty() || viewModel.isUpdatingCache

    var selectedApp by remember { mutableStateOf<App?>(null) }
    var isAppInfoVisible by remember { mutableStateOf(false) }
    var watchConnectionWarningVisible by remember(isLoading, isWatchConnected) {
        mutableStateOf(!isLoading && !isWatchConnected)
    }

    BackHandler(enabled = isAppInfoVisible) {
        isAppInfoVisible = false
    }

    Column(
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
            userApps = userApps,
            disabledApps = disabledApps,
            systemApps = systemApps,
            onAppClick = { app ->
                selectedApp = app
                isAppInfoVisible = true
            }
        )
    }

    AnimatedVisibility(
        visible = isAppInfoVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        AppInfo(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(16.dp),
            app = selectedApp,
            onOpenClicked = onOpenClicked,
            onUninstallClicked = {
                onUninstallClicked(it)
                isAppInfoVisible = false
            }
        )
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

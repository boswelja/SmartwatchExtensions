package com.boswelja.smartwatchextensions.appmanager.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import timber.log.Timber

class AppManagerActivity : AppCompatActivity() {

    @FlowPreview
    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val continueOnWatchText =
                stringResource(R.string.watch_manager_action_continue_on_watch)
            val viewModel: AppManagerViewModel = viewModel()

            val registeredWatches by viewModel.registeredWatches.observeAsState()

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

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun AppManagerScreen(
    onOpenClicked: (App) -> Unit,
    onUninstallClicked: (App) -> Unit
) {
    val viewModel: AppManagerViewModel = viewModel()

    val userApps by viewModel.userApps.collectAsState(emptyList(), Dispatchers.IO)
    val systemApps by viewModel.systemApps.collectAsState(emptyList(), Dispatchers.IO)
    val isWatchConnected by viewModel.isWatchConnected.collectAsState(true, Dispatchers.IO)

    var selectedApp by remember { mutableStateOf<App?>(null) }
    var isAppInfoVisible by remember { mutableStateOf(false) }

    BackHandler(enabled = isAppInfoVisible) {
        isAppInfoVisible = false
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CacheStatusIndicator(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            isUpdatingCache = viewModel.isUpdatingCache
        )
        WatchStatusIndicator(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            isWatchConnected = isWatchConnected
        )
        AppList(
            userApps = userApps,
            systemApps = systemApps,
            onAppClick = { app ->
                selectedApp = app
                isAppInfoVisible = true
            }
        )
    }

    AnimatedVisibility(
        visible = isAppInfoVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
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

@ExperimentalAnimationApi
@Composable
fun CacheStatusIndicator(
    modifier: Modifier = Modifier,
    isUpdatingCache: Boolean
) {
    AnimatedVisibility(
        visible = isUpdatingCache,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(Modifier.fillMaxWidth()) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            StatusItem(
                modifier = modifier,
                icon = Icons.Outlined.Info,
                text = stringResource(R.string.app_manager_updating_cache)
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun WatchStatusIndicator(
    modifier: Modifier = Modifier,
    isWatchConnected: Boolean
) {
    AnimatedVisibility(
        visible = !isWatchConnected,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        StatusItem(
            modifier = modifier,
            icon = Icons.Outlined.Warning,
            text = stringResource(R.string.app_manager_watch_disconnected)
        )
    }
}

@Composable
fun StatusItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null
        )
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

package com.boswelja.smartwatchextensions.appmanager.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.Crossflow
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import com.boswelja.watchconnection.core.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber

class AppManagerActivity : AppCompatActivity() {

    private var currentDestination by mutableStateOf(Destination.APP_LIST)

    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")

        setContent {
            val viewModel: AppManagerViewModel = viewModel()

            val registeredWatches by viewModel.registeredWatches.observeAsState()
            val state by viewModel.watchStatus.collectAsState(Status.CONNECTING, Dispatchers.IO)

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
                    AppManagerScreen(scaffoldState = scaffoldState)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (currentDestination == Destination.APP_INFO) {
            currentDestination = Destination.APP_LIST
        } else {
            super.onBackPressed()
        }
    }
}

private enum class Destination {
    APP_LIST,
    APP_INFO
}

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun AppManagerScreen(scaffoldState: ScaffoldState) {
    val scope = rememberCoroutineScope()
    val continueOnWatchText = stringResource(R.string.watch_manager_action_continue_on_watch)
    var destination by remember { mutableStateOf(Destination.APP_INFO) }
    var selectedApp by remember { mutableStateOf<App?>(null) }
    val viewModel: AppManagerViewModel = viewModel()
    Column {
        if (viewModel.isUpdatingCache) {
            LinearProgressIndicator(
                Modifier.fillMaxWidth()
            )
        }
        Crossflow(targetState = destination) {
            when (it) {
                Destination.APP_LIST -> AppList(
                    viewModel,
                    onAppClick = { app ->
                        selectedApp = app
                        destination = Destination.APP_INFO
                    }
                )
                Destination.APP_INFO -> AppInfo(
                    app = selectedApp,
                    onOpenClicked = {
                        scope.launch {
                            if (viewModel.sendOpenRequest(it)) {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    continueOnWatchText, null
                                )
                            }
                        }
                    },
                    onUninstallClicked = {
                        scope.launch {
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

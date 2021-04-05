package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.Crossflow
import com.boswelja.devicemanager.common.ui.LoadingScreen
import com.boswelja.devicemanager.common.ui.UpNavigationWatchPickerAppBar
import kotlinx.coroutines.launch
import timber.log.Timber

class AppManagerActivity : AppCompatActivity() {

    private var currentDestination by mutableStateOf(Destination.LOADING)

    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")

        setContent {
            val viewModel: AppManagerViewModel = viewModel()
            val state by viewModel.state.observeAsState()

            val selectedWatch by viewModel.watchManager.selectedWatch.observeAsState()
            val registeredWatches by viewModel.watchManager.registeredWatches.observeAsState()

            val scaffoldState = rememberScaffoldState()

            AppTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationWatchPickerAppBar(
                            selectedWatch = selectedWatch,
                            watches = registeredWatches,
                            onWatchSelected = { viewModel.watchManager.selectWatchById(it.id) },
                            onNavigateUp = { onBackPressed() }
                        )
                    }
                ) {
                    AppManagerScreen(
                        scaffoldState = scaffoldState,
                        currentDestination = currentDestination
                    )
                }
            }

            when (state) {
                State.CONNECTING, State.LOADING_APPS -> currentDestination = Destination.LOADING
                State.READY -> currentDestination = Destination.APP_LIST
                State.DISCONNECTED -> {
                    val scope = rememberCoroutineScope()
                    scope.launch {
                        val result = scaffoldState.snackbarHostState
                            .showSnackbar(
                                getString(R.string.app_manager_disconnected),
                                getString(R.string.button_retry),
                                SnackbarDuration.Indefinite
                            )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.startAppManagerService()
                        }
                    }
                }
                State.ERROR -> currentDestination = Destination.ERROR
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

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun AppManagerScreen(scaffoldState: ScaffoldState, currentDestination: Destination) {
        var selectedApp by remember { mutableStateOf<App?>(null) }
        val viewModel: AppManagerViewModel = viewModel()
        Crossflow(targetState = currentDestination) {
            when (it) {
                Destination.LOADING -> {
                    val progress by viewModel.progress.observeAsState()
                    LoadingScreen((progress ?: 0) / 100f)
                }
                Destination.APP_LIST -> AppList(
                    viewModel,
                    onAppClick = { app ->
                        selectedApp = app
                        this.currentDestination = Destination.APP_INFO
                    }
                )
                Destination.APP_INFO -> AppInfo(
                    selectedApp,
                    scaffoldState,
                    viewModel
                )
                Destination.ERROR -> Error(viewModel)
            }
        }
    }

    enum class Destination {
        LOADING,
        APP_LIST,
        APP_INFO,
        ERROR
    }
}

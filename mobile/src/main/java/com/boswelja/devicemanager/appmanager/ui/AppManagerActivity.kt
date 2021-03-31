package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import androidx.navigation.compose.rememberNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.LoadingScreen
import com.boswelja.devicemanager.common.ui.UpNavigationWatchPickerAppBar
import kotlinx.coroutines.launch
import timber.log.Timber

class AppManagerActivity : AppCompatActivity() {

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")

        setContent {
            val viewModel: AppManagerViewModel = viewModel()
            val progress by viewModel.progress.observeAsState()
            val state by viewModel.state.observeAsState()

            val selectedWatch by viewModel.watchManager.selectedWatch.observeAsState()
            val registeredWatches by viewModel.watchManager.registeredWatches.observeAsState()

            val scaffoldState = rememberScaffoldState()
            val navController = rememberNavController()

            AppTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationWatchPickerAppBar(
                            selectedWatch = selectedWatch,
                            watches = registeredWatches,
                            onWatchSelected = { viewModel.watchManager.selectWatchById(it.id) },
                            onNavigateUp = {
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                } else {
                                    finish()
                                }
                            }
                        )
                    }
                ) {
                    NavHost(navController, startDestination = LOADING) {
                        composable(LOADING) { LoadingScreen((progress ?: 0) / 100f) }
                        composable(ERROR) { Error(viewModel) }
                        composable(APP_LIST) {
                            AppList(
                                viewModel,
                                onAppClick = {
                                    navController.currentBackStackEntry?.arguments
                                        ?.putSerializable("app", it)
                                    navController.navigateTo(APP_INFO)
                                }
                            )
                        }
                        composable(APP_INFO) {
                            val app = navController.previousBackStackEntry?.arguments
                                ?.getSerializable("app") as App
                            AppInfo(
                                app,
                                scaffoldState,
                                viewModel
                            )
                        }
                    }
                }
            }

            when (state) {
                State.CONNECTING, State.LOADING_APPS -> navController.navigateTo(LOADING)
                State.READY -> navController.navigateTo(APP_LIST)
                State.DISCONNECTED -> Disconnected(scaffoldState)
                State.ERROR -> navController.navigateTo(ERROR)
            }
        }
    }

    @Composable
    private fun Disconnected(scaffoldState: ScaffoldState) {
        val scope = rememberCoroutineScope()
        val viewModel: AppManagerViewModel = viewModel()
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

    private fun NavHostController.navigateTo(route: String) {
        try {
            navigate(route) {
                launchSingleTop = true
                if (route != APP_INFO) {
                    popUpTo(LOADING) {
                        inclusive = true
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w("Problem with nav controller, aborting")
        }
    }

    companion object Destinations {
        private const val LOADING = "loading"
        private const val APP_LIST = "applist"
        private const val APP_INFO = "appinfo"
        private const val ERROR = "error"
    }
}

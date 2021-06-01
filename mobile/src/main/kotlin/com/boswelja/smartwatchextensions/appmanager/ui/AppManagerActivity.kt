package com.boswelja.smartwatchextensions.appmanager.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.Crossflow
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

            val selectedWatch by viewModel.selectedWatch.observeAsState()
            val registeredWatches by viewModel.registeredWatches.observeAsState()
            val state by viewModel.watchStatus.observeAsState()

            val scaffoldState = rememberScaffoldState()

            AppTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationWatchPickerAppBar(
                            selectedWatch = selectedWatch,
                            watches = registeredWatches,
                            onWatchSelected = { viewModel.selectWatchById(it.id) },
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
        }
    }

    override fun onBackPressed() {
        if (currentDestination == Destination.APP_INFO) {
            currentDestination = Destination.APP_LIST
        } else {
            super.onBackPressed()
        }
    }

    @ExperimentalCoroutinesApi
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun AppManagerScreen(scaffoldState: ScaffoldState, currentDestination: Destination) {
        var selectedApp by remember { mutableStateOf<App?>(null) }
        val viewModel: AppManagerViewModel = viewModel()
        Crossflow(targetState = currentDestination) {
            when (it) {
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
            }
        }
    }

    enum class Destination {
        APP_LIST,
        APP_INFO
    }
}

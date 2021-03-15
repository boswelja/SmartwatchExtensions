package com.boswelja.devicemanager.managespace.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import kotlinx.coroutines.launch

/**
 * An activity to present the user with options for resetting various data used by Wearable
 * Extensions.
 */
class ManageSpaceActivity : AppCompatActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                var progress by mutableStateOf(0f)
                val scaffoldState = rememberScaffoldState()
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationAppBar(
                            title = { Text(stringResource(R.string.manage_space_title)) },
                            onNavigateUp = { finish() }
                        )
                    }
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .scrollable(rememberScrollState(), Orientation.Vertical)
                    ) {
                        ClearCacheAction(
                            scaffoldState = scaffoldState,
                            onProgressChange = { progress = it }
                        )
                        ResetAnalyticsAction(scaffoldState = scaffoldState)
                        ResetAppSettingsAction(
                            scaffoldState = scaffoldState,
                            onProgressChange = { progress = it }
                        )
                        ResetExtensionsAction(
                            scaffoldState = scaffoldState,
                            onProgressChange = { progress = it }
                        )
                        ResetWearableExtensionsAction(
                            scaffoldState = scaffoldState,
                            onProgressChange = { progress = it }
                        )
                    }
                    if (progress > 0.0f) {
                        AlertDialog(
                            onDismissRequest = { if (progress >= 1f) progress = 0f },
                            title = { Text(stringResource(R.string.please_wait)) },
                            text = { LinearProgressIndicator(progress, Modifier.fillMaxWidth()) },
                            buttons = { }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Action(
        title: String,
        desc: String,
        buttonLabel: String,
        onButtonClick: () -> Unit
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6
            )
            Text(
                desc,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1
            )
            OutlinedButton(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonLabel)
            }
        }
    }

    @Composable
    fun RequestDialog(
        title: String,
        text: String,
        onRequestGranted: () -> Unit,
        onRequestDenied: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onRequestDenied,
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = onRequestGranted) {
                    Text(stringResource(R.string.dialog_button_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = onRequestDenied) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }

    @ExperimentalMaterialApi
    @Composable
    fun ClearCacheAction(
        scaffoldState: ScaffoldState,
        onProgressChange: (Float) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val viewModel: ManageSpaceViewModel = viewModel()
        Action(
            title = stringResource(R.string.clear_cache_title),
            desc = stringResource(R.string.clear_cache_desc),
            buttonLabel = stringResource(R.string.clear_cache_title),
            onButtonClick = {
                viewModel.clearCache(
                    { onProgressChange(it / 100f) },
                    {
                        onProgressChange(0f)
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                if (it) getString(R.string.clear_cache_success)
                                else getString(R.string.clear_cache_failed)
                            )
                        }
                    }
                )
            }
        )
    }

    @ExperimentalMaterialApi
    @Composable
    fun ResetAnalyticsAction(scaffoldState: ScaffoldState) {
        val scope = rememberCoroutineScope()
        val viewModel: ManageSpaceViewModel = viewModel()
        Action(
            title = stringResource(R.string.reset_analytics_title),
            desc = stringResource(R.string.reset_analytics_desc),
            buttonLabel = stringResource(R.string.reset_analytics_title),
            onButtonClick = {
                viewModel.resetAnalytics {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            if (it) getString(R.string.reset_analytics_success)
                            else getString(R.string.reset_analytics_failed)
                        )
                    }
                }
            }
        )
    }

    @ExperimentalMaterialApi
    @Composable
    fun ResetAppSettingsAction(
        scaffoldState: ScaffoldState,
        onProgressChange: (Float) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val viewModel: ManageSpaceViewModel = viewModel()
        var dialogVisible by remember { mutableStateOf(false) }
        Action(
            title = stringResource(R.string.reset_settings_title),
            desc = stringResource(R.string.reset_settings_desc),
            buttonLabel = stringResource(R.string.reset_settings_title),
            onButtonClick = { dialogVisible = true }
        )
        if (dialogVisible) {
            RequestDialog(
                title = stringResource(R.string.reset_settings_title),
                text = stringResource(R.string.reset_settings_desc),
                onRequestGranted = {
                    dialogVisible = false
                    viewModel.resetAppSettings(
                        { onProgressChange(it / 100f) },
                        {
                            onProgressChange(0f)
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    if (it) getString(R.string.reset_settings_success)
                                    else getString(R.string.reset_settings_failed)
                                )
                            }
                        }
                    )
                },
                onRequestDenied = { dialogVisible = false }
            )
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun ResetExtensionsAction(
        scaffoldState: ScaffoldState,
        onProgressChange: (Float) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val viewModel: ManageSpaceViewModel = viewModel()
        var dialogVisible by remember { mutableStateOf(false) }
        Action(
            title = stringResource(R.string.reset_extensions_title),
            desc = stringResource(R.string.reset_extensions_desc),
            buttonLabel = stringResource(R.string.reset_extensions_title),
            onButtonClick = { dialogVisible = true }
        )
        if (dialogVisible) {
            RequestDialog(
                title = stringResource(R.string.reset_extensions_title),
                text = stringResource(R.string.reset_extensions_desc),
                onRequestGranted = {
                    dialogVisible = false
                    viewModel.resetExtensionSettings(
                        { onProgressChange(it / 100f) },
                        {
                            onProgressChange(0f)
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    if (it) getString(R.string.reset_extensions_success)
                                    else getString(R.string.reset_extensions_failed)
                                )
                            }
                        }
                    )
                },
                onRequestDenied = { dialogVisible = false }
            )
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun ResetWearableExtensionsAction(
        scaffoldState: ScaffoldState,
        onProgressChange: (Float) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val viewModel: ManageSpaceViewModel = viewModel()
        var dialogVisible by remember { mutableStateOf(false) }
        Action(
            title = stringResource(R.string.reset_app_title),
            desc = stringResource(R.string.reset_app_desc),
            buttonLabel = stringResource(R.string.reset_app_title),
            onButtonClick = { dialogVisible = true }
        )
        if (dialogVisible) {
            RequestDialog(
                title = stringResource(R.string.reset_app_title),
                text = stringResource(R.string.reset_app_desc),
                onRequestGranted = {
                    dialogVisible = false
                    viewModel.resetApp(
                        { onProgressChange(it / 100f) },
                        {
                            onProgressChange(0f)
                            if (it) finish()
                            else {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        getString(R.string.reset_app_failed)
                                    )
                                }
                            }
                        }
                    )
                },
                onRequestDenied = { dialogVisible = false }
            )
        }
    }
}

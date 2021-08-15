package com.boswelja.smartwatchextensions.managespace.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import kotlinx.coroutines.launch

@Composable
fun Action(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    buttonLabel: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = modifier,
        header = {
            CardHeader(title = { Text(title) })
        }
    ) {
        Column {
            Text(
                text = desc,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            TextButton(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
fun ClearCacheAction(
    scaffoldState: ScaffoldState,
    onProgressChange: (Float) -> Unit
) {
    val context = LocalContext.current
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
                            if (it) context.getString(R.string.clear_cache_success)
                            else context.getString(R.string.clear_cache_failed)
                        )
                    }
                }
            )
        }
    )
}

@Composable
fun ResetAnalyticsAction(scaffoldState: ScaffoldState) {
    val context = LocalContext.current
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
                        if (it) context.getString(R.string.reset_analytics_success)
                        else context.getString(R.string.reset_analytics_failed)
                    )
                }
            }
        }
    )
}

@Composable
fun ResetAppSettingsAction(
    scaffoldState: ScaffoldState,
    onProgressChange: (Float) -> Unit
) {
    val context = LocalContext.current
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
                viewModel.resetAppSettings {
                    onProgressChange(0f)
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            if (it) context.getString(R.string.reset_settings_success)
                            else context.getString(R.string.reset_settings_failed)
                        )
                    }
                }
            },
            onRequestDenied = { dialogVisible = false }
        )
    }
}

@Composable
fun ResetExtensionsAction(
    scaffoldState: ScaffoldState,
    onProgressChange: (Float) -> Unit
) {
    val context = LocalContext.current
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
            text = stringResource(R.string.reset_extensions_warning),
            onRequestGranted = {
                dialogVisible = false
                viewModel.resetExtensionSettings(
                    { onProgressChange(it / 100f) },
                    {
                        onProgressChange(0f)
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                if (it) context.getString(R.string.reset_extensions_success)
                                else context.getString(R.string.reset_extensions_failed)
                            )
                        }
                    }
                )
            },
            onRequestDenied = { dialogVisible = false }
        )
    }
}

@Composable
fun ResetAppAction(
    scaffoldState: ScaffoldState,
    onProgressChange: (Float) -> Unit
) {
    val context = LocalContext.current
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
            text = stringResource(R.string.reset_app_warning),
            onRequestGranted = {
                dialogVisible = false
                viewModel.resetApp(
                    { onProgressChange(it / 100f) },
                    {
                        onProgressChange(0f)
                        if (it) {
                            // finish()
                        } else {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    context.getString(R.string.reset_app_failed)
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

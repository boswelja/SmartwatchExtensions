package com.boswelja.smartwatchextensions.managespace.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import org.koin.androidx.compose.getViewModel

private const val PROGRESS_DIVISOR = 100f

/**
 * A base Action Composable for [ManageSpaceScreen].
 * @param modifier [Modifier].
 * @param title The action title.
 * @param desc The action description.
 * @param buttonLabel The action button label.
 * @param onButtonClick Called when the action button is clicked.
 */
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
        header = { CardHeader(title = { Text(title) }) }
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
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * An [Action] for clearing cache.
 * @param modifier [Modifier].
 * @param onActionFinished Called when the action has finished running.
 * @param onProgressChange Called when the action progress changes.
 */
@Composable
fun ClearCacheAction(
    modifier: Modifier = Modifier,
    onActionFinished: (success: Boolean) -> Unit,
    onProgressChange: (Float) -> Unit
) {
    val viewModel: ManageSpaceViewModel = getViewModel()
    Action(
        modifier = modifier,
        title = stringResource(R.string.clear_cache_title),
        desc = stringResource(R.string.clear_cache_desc),
        buttonLabel = stringResource(R.string.clear_cache_title),
        onButtonClick = {
            viewModel.clearCache(
                { onProgressChange(it / PROGRESS_DIVISOR) },
                { onActionFinished(it) }
            )
        }
    )
}

/**
 * An [Action] for resetting app settings.
 * @param modifier [Modifier].
 * @param onActionFinished Called when the action has finished executing.
 * @param onProgressChange Called when the progress has changed.
 */
@Composable
fun ResetAppSettingsAction(
    modifier: Modifier = Modifier,
    onActionFinished: (success: Boolean) -> Unit,
    onProgressChange: (Float) -> Unit
) {
    val viewModel: ManageSpaceViewModel = getViewModel()
    var dialogVisible by remember { mutableStateOf(false) }
    Action(
        modifier = modifier,
        title = stringResource(R.string.reset_settings_title),
        desc = stringResource(R.string.reset_settings_desc),
        buttonLabel = stringResource(R.string.reset_settings_title),
        onButtonClick = { dialogVisible = true }
    )
    if (dialogVisible) {
        AlertDialog(
            title = { Text(stringResource(R.string.reset_settings_title)) },
            text = { Text(stringResource(R.string.reset_settings_desc)) },
            onDismissRequest = { dialogVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogVisible = false
                        viewModel.resetAppSettings {
                            onProgressChange(1f)
                            onActionFinished(it)
                        }
                    }
                ) { Text(stringResource(R.string.dialog_button_reset)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }
}

/**
 * An [Action] for resetting extension settings.
 * @param modifier [Modifier].
 * @param onActionFinished Called when the action has finished executing.
 * @param onProgressChange Called when the progress has changed.
 */
@Composable
fun ResetExtensionsAction(
    modifier: Modifier = Modifier,
    onActionFinished: (success: Boolean) -> Unit,
    onProgressChange: (Float) -> Unit
) {
    val viewModel: ManageSpaceViewModel = getViewModel()
    var dialogVisible by remember { mutableStateOf(false) }
    Action(
        modifier = modifier,
        title = stringResource(R.string.reset_extensions_title),
        desc = stringResource(R.string.reset_extensions_desc),
        buttonLabel = stringResource(R.string.reset_extensions_title),
        onButtonClick = { dialogVisible = true }
    )
    if (dialogVisible) {
        AlertDialog(
            title = { Text(stringResource(R.string.reset_extensions_title)) },
            text = { Text(stringResource(R.string.reset_extensions_warning)) },
            onDismissRequest = { dialogVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogVisible = false
                        viewModel.resetExtensionSettings(
                            { onProgressChange(it / PROGRESS_DIVISOR) },
                            { onActionFinished(it) }
                        )
                    }
                ) { Text(stringResource(R.string.dialog_button_reset)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }
}

/**
 * An [Action] for resetting the app.
 * @param modifier [Modifier].
 * @param onActionFinished Called when the action has finished executing.
 * @param onProgressChange Called when the progress has changed.
 */
@Composable
fun ResetAppAction(
    modifier: Modifier = Modifier,
    onActionFinished: (success: Boolean) -> Unit,
    onProgressChange: (Float) -> Unit
) {
    val viewModel: ManageSpaceViewModel = getViewModel()
    var dialogVisible by remember { mutableStateOf(false) }
    Action(
        modifier = modifier,
        title = stringResource(R.string.reset_app_title),
        desc = stringResource(R.string.reset_app_desc),
        buttonLabel = stringResource(R.string.reset_app_title),
        onButtonClick = { dialogVisible = true }
    )
    if (dialogVisible) {
        AlertDialog(
            title = { Text(stringResource(R.string.reset_app_title)) },
            text = { Text(stringResource(R.string.reset_app_warning)) },
            onDismissRequest = { dialogVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogVisible = false
                        viewModel.resetApp(
                            { onProgressChange(it / PROGRESS_DIVISOR) },
                            { onActionFinished(it) }
                        )
                    }
                ) { Text(stringResource(R.string.dialog_button_reset)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }
}

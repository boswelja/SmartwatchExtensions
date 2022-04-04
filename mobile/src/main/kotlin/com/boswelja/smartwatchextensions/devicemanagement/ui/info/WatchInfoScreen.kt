package com.boswelja.smartwatchextensions.devicemanagement.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

/**
 * A screen displaying information & actions related to the given watch.
 * @param contentPadding The screen padding.
 * @param watch The watch to show info & actions for.
 * @param onShowSnackbar Called when a snackbar should be displayed.
 * @param onWatchRemoved Called when the watch was removed.
 * @param modifier [Modifier].
 */
@Composable
fun WatchInfoScreen(
    contentPadding: PaddingValues,
    watch: Watch,
    onShowSnackbar: suspend (String) -> Unit,
    onWatchRemoved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: WatchInfoViewModel = getViewModel()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = watch) {
        viewModel.getCapabilities(watch)
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WatchActions(
            watchName = watch.name,
            onResetSettings = {
                viewModel.resetWatchPreferences(watch)
                scope.launch {
                    onShowSnackbar(context.getString(R.string.clear_preferences_success))
                }
            },
            onForgetWatch = {
                viewModel.forgetWatch(watch)
                onWatchRemoved()
            }
        )
        WatchDetailsCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            watch = watch,
            capabilities = viewModel.watchCapabilities,
            onNicknameChanged = { scope.launch { viewModel.updateWatchName(watch, it) } }
        )
    }
}

/**
 * A card to display details for a given watch.
 * @param modifier [Modifier].
 * @param contentPadding The card content padding.
 * @param watch The watch to displat details for.
 * @param capabilities The watches capabilities.
 * @param onNicknameChanged Called when the watch nickname changes.
 */
@Composable
fun WatchDetailsCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    watch: Watch,
    capabilities: List<String>,
    onNicknameChanged: (String) -> Unit
) {
    Column(
        modifier.padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WatchNameField(
            watchName = watch.name,
            onWatchNameChanged = onNicknameChanged
        )
        WatchCapabilities(capabilities = capabilities)
    }
}

/**
 * A TextField to allow editing a watches name.
 * @param modifier [Modifier].
 * @param debounce The debounce between data updates.
 * @param watchName The current watch name.
 * @param onWatchNameChanged Called when the watch name has changed. This will be called in
 * intervals no sooner than [debounce] milliseconds while the user is editing.
 */
@Composable
fun WatchNameField(
    modifier: Modifier = Modifier,
    debounce: Long = 350,
    watchName: String,
    onWatchNameChanged: (String) -> Unit
) {
    var currentName by rememberSaveable(watchName) { mutableStateOf(watchName) }
    val nameValid = currentName.isNotBlank()

    OutlinedTextField(
        modifier = modifier,
        value = currentName,
        singleLine = true,
        onValueChange = {
            currentName = it
        },
        keyboardActions = KeyboardActions {
            if (nameValid && watchName != currentName) {
                onWatchNameChanged(currentName)
            }
        }
    )

    // Only start the debounce if the watch name is both valid and altered
    if (nameValid && watchName != currentName) {
        // Debounce for watch name updates, ensuring we only write data when needed
        LaunchedEffect(currentName) {
            delay(debounce)
            onWatchNameChanged(currentName)
        }
    }
}

/**
 * A list of capabilities.
 * @param modifier [Modifier].
 * @param capabilities The list of capabilities to display.
 */
@Composable
fun WatchCapabilities(
    modifier: Modifier = Modifier,
    capabilities: List<String>
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.capabilities_title))
        capabilities.forEach { capability ->
            Text(capability)
        }
    }
}

/**
 * A row of actions that can be taken for a watch.
 * @param modifier [Modifier].
 * @param contentSpacing The content spacing.
 * @param watchName The name of the watch the actions are for.
 * @param onResetSettings Called when reset settings is clicked.
 * @param onForgetWatch Called when forget watch is selected.
 */
@Composable
fun WatchActions(
    modifier: Modifier = Modifier,
    contentSpacing: Dp = 2.dp,
    watchName: String,
    onResetSettings: () -> Unit,
    onForgetWatch: () -> Unit
) {
    var clearSettingsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var forgetWatchDialogVisible by rememberSaveable { mutableStateOf(false) }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(contentSpacing)) {
        FilledTonalButton(onClick = { clearSettingsDialogVisible = true }) {
            Icon(Icons.Default.ClearAll, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.clear_preferences_button_text))
        }
        FilledTonalButton(onClick = { forgetWatchDialogVisible = true }) {
            Icon(Icons.Default.Delete, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.button_forget_watch))
        }
    }
    if (clearSettingsDialogVisible) {
        ResetSettingsDialog(
            watchName = watchName,
            onDismissRequest = { clearSettingsDialogVisible = false },
            onResetSettings = {
                onResetSettings()
                clearSettingsDialogVisible = false
            }
        )
    }
    if (forgetWatchDialogVisible) {
        ForgetWatchDialog(
            watchName = watchName,
            onDismissRequest = { forgetWatchDialogVisible = false },
            onForgetWatch = onForgetWatch
        )
    }
}

/**
 * A dialog to request deregistering a watch.
 * @param modifier [Modifier].
 * @param watchName The name of the watch whose extension settings will be cleared.
 * @param onDismissRequest Called when the dialog should be dismissed.
 * @param onForgetWatch Called when the deregister request is accepted.
 */
@Composable
fun ForgetWatchDialog(
    modifier: Modifier = Modifier,
    watchName: String,
    onDismissRequest: () -> Unit,
    onForgetWatch: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.forget_watch_dialog_title)) },
        text = {
            Text(stringResource(R.string.forget_watch_dialog_message, watchName, watchName))
        },
        confirmButton = {
            TextButton(onClick = onForgetWatch) {
                Text(stringResource(R.string.button_forget_watch))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

/**
 * A dialog to request resetting extension settings for a watch.
 * @param modifier [Modifier].
 * @param watchName The name of the watch whose extension settings will be cleared.
 * @param onDismissRequest Called when the dialog should be dismissed.
 * @param onResetSettings Called when the reset request is accepted.
 */
@Composable
fun ResetSettingsDialog(
    modifier: Modifier = Modifier,
    watchName: String,
    onDismissRequest: () -> Unit,
    onResetSettings: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.clear_preferences_dialog_title)) },
        text = {
            Text(stringResource(R.string.clear_preferences_dialog_message, watchName))
        },
        confirmButton = {
            TextButton(onClick = onResetSettings) {
                Text(stringResource(R.string.dialog_button_reset))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

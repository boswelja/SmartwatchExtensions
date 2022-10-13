package com.boswelja.smartwatchextensions.watchmanager.ui.manageregistered

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.watchmanager.R
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ManageRegisteredWatchScreen(
    modifier: Modifier = Modifier,
    viewModel: ManageRegisteredWatchViewModel = getViewModel()
) {
    val watch by viewModel.watch.collectAsState()

    AnimatedContent(
        targetState = watch,
        transitionSpec = { fadeIn() with fadeOut() }
    ) {
        if (it == null) {
            LoadingScreen(modifier = modifier)
        } else {
            val watchStatus by viewModel.watchStatus.collectAsState()
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .then(modifier),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WatchOverview(
                    watchStatus = watchStatus,
                    modifier = Modifier.fillMaxWidth()
                )
                RenameWatch(
                    watchName = it.name,
                    onUpdateWatchName = viewModel::renameWatch,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResetWatchSettings(
                        watchName = it.name,
                        onResetSettings = viewModel::resetWatchSettings,
                        modifier = Modifier.fillMaxWidth()
                    )
                    RemoveWatch(
                        watchName = it.name,
                        onWatchRemoved = viewModel::removeWatch,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
internal fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun WatchOverview(
    watchStatus: ConnectionMode?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            Icons.Default.Watch,
            null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
        )
        Spacer(Modifier.height(8.dp))
        AnimatedContent(targetState = watchStatus) {
            if (it != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(it.getIcon(), null)
                    Spacer(Modifier.width(8.dp))
                    Text(it.getText())
                }
            } else {
                LinearProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenameWatch(
    watchName: String,
    onUpdateWatchName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var updatedName by remember(watchName) { mutableStateOf(watchName) }
    val isUpdatedNameValid by remember(updatedName) {
        derivedStateOf { updatedName.isNotBlank() }
    }
    val canSaveName by remember(watchName, updatedName, isUpdatedNameValid) {
        derivedStateOf { isUpdatedNameValid && watchName != updatedName }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = updatedName,
            onValueChange = { updatedName = it },
            isError = !isUpdatedNameValid,
            label = { Text(stringResource(R.string.watch_rename_hint)) },
            trailingIcon = {
                AnimatedVisibility(
                    visible = updatedName != watchName,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { updatedName = watchName }) {
                        Icon(Icons.Default.Restore, stringResource(R.string.watch_rename_restore))
                    }
                }
            },
            keyboardActions = KeyboardActions(
                onDone = { onUpdateWatchName(updatedName) }
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = { onUpdateWatchName(updatedName) },
            enabled = canSaveName
        ) {
            Icon(Icons.Default.Save, stringResource(R.string.watch_rename_save))
        }
    }
}

@Composable
internal fun RemoveWatch(
    watchName: String,
    onWatchRemoved: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmDeleteVisible by remember { mutableStateOf(false) }
    FilledTonalButton(
        onClick = { confirmDeleteVisible = true },
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = modifier
    ) {
        Icon(Icons.Default.Delete, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.watch_delete_btn))
    }
    if (confirmDeleteVisible) {
        AlertDialog(
            onDismissRequest = { confirmDeleteVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onWatchRemoved()
                        confirmDeleteVisible = false
                    }
                ) { Text(stringResource(R.string.watch_delete_positive)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDeleteVisible = false }
                ) { Text(stringResource(R.string.watch_delete_negative)) }
            },
            title = { Text(stringResource(R.string.watch_delete_title, watchName)) },
            text = { Text(stringResource(R.string.watch_delete_text, watchName)) },
            icon = { Icon(Icons.Default.Delete, null) }
        )
    }
}

@Composable
internal fun ResetWatchSettings(
    watchName: String,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmDeleteVisible by remember { mutableStateOf(false) }
    FilledTonalButton(
        onClick = { confirmDeleteVisible = true },
        colors = ButtonDefaults.filledTonalButtonColors(),
        modifier = modifier
    ) {
        Icon(Icons.Default.ClearAll, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.watch_reset_btn))
    }
    if (confirmDeleteVisible) {
        AlertDialog(
            onDismissRequest = { confirmDeleteVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetSettings()
                        confirmDeleteVisible = false
                    }
                ) { Text(stringResource(R.string.watch_reset_positive)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDeleteVisible = false }
                ) { Text(stringResource(R.string.watch_reset_negative)) }
            },
            title = { Text(stringResource(R.string.watch_reset_title, watchName)) },
            text = { Text(stringResource(R.string.watch_reset_text, watchName)) },
            icon = { Icon(Icons.Default.ClearAll, null) }
        )
    }
}

private fun ConnectionMode.getIcon(): ImageVector {
    return when (this) {
        ConnectionMode.Disconnected -> Icons.Default.CloudOff
        ConnectionMode.Internet -> Icons.Default.CloudDone
        ConnectionMode.Bluetooth -> Icons.Default.BluetoothConnected
    }
}

@Composable
private fun ConnectionMode.getText(): String {
    return when (this) {
        ConnectionMode.Disconnected -> stringResource(R.string.watch_status_disconnected)
        ConnectionMode.Internet,
        ConnectionMode.Bluetooth -> stringResource(R.string.watch_status_connected)
    }
}

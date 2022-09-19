package com.boswelja.smartwatchextensions.devicemanagement.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.core.ui.list.ListItem
import com.boswelja.smartwatchextensions.core.ui.snackbarVisuals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying Watch Manager actions.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onShowSnackbar Called when a snackbar should be shown.
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun WatchManagerScreen(
    onShowSnackbar: suspend (SnackbarVisuals) -> Unit,
    onNavigateTo: (WatchManagerDestination) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: WatchManagerViewModel = getViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val registeredWatches by viewModel.registeredWatches.collectAsState(emptyList(), Dispatchers.IO)

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(registeredWatches) { watch ->
            RegisteredWatchItem(
                watchName = watch.name,
                onSyncCapabilities = {
                    viewModel.syncCapabilities(watch)
                    coroutineScope.launch {
                        onShowSnackbar(
                            snackbarVisuals(message = context.getString(R.string.watch_manager_sync_in_progress))
                        )
                    }
                },
                onRename = { viewModel.renameWatch(watch, it) },
                onForget = { viewModel.forgetWatch(watch) }
            )
        }
        item {
            ListItem(
                text = { Text(stringResource(R.string.watch_manager_add_watch_title)) },
                icon = { Icon(Icons.Default.Add, null) },
                modifier = Modifier.clickable {
                    onNavigateTo(WatchManagerDestination.REGISTER_WATCHES)
                }
            )
        }
    }
}

/**
 * Displays information about a watch that was registered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisteredWatchItem(
    watchName: String,
    onSyncCapabilities: () -> Unit,
    onRename: (String) -> Unit,
    onForget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var forgetDialogVisible by remember { mutableStateOf(false) }
    var renameDialogVisible by remember { mutableStateOf(false) }
    ListItem(
        text = { Text(watchName) },
        icon = { Icon(Icons.Outlined.Watch, null) },
        trailing = {
            var menuVisible by remember { mutableStateOf(false) }
            Box(Modifier.wrapContentSize()) {
                IconButton(onClick = { menuVisible = true }) { Icon(Icons.Default.MoreVert, null) }
                DropdownMenu(expanded = menuVisible, onDismissRequest = { menuVisible = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.watch_manager_sync)) },
                        onClick = onSyncCapabilities
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.watch_manager_rename)) },
                        onClick = { renameDialogVisible = true }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.watch_manager_forget)) },
                        onClick = { forgetDialogVisible = true }
                    )
                }
            }
        },
        modifier = modifier
    )
    if (forgetDialogVisible) {
        AlertDialog(
            onDismissRequest = { forgetDialogVisible = false },
            title = { Text(stringResource(R.string.forget_watch_dialog_title)) },
            text = { Text(stringResource(R.string.forget_watch_dialog_message, watchName, watchName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onForget()
                        forgetDialogVisible = false
                    }
                ) { Text(stringResource(R.string.button_forget_watch)) }
            },
            dismissButton = {
                TextButton(onClick = { forgetDialogVisible = false }) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }
    if (renameDialogVisible) {
        var newName by remember(watchName) { mutableStateOf(watchName) }
        val nameValid = remember(newName) { newName.isNotBlank() }
        AlertDialog(
            onDismissRequest = { renameDialogVisible = false },
            title = { Text(stringResource(R.string.watch_manager_rename)) },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    isError = !nameValid,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = nameValid,
                    onClick = {
                        onRename(newName)
                        renameDialogVisible = false
                    }
                ) { Text(stringResource(R.string.watch_manager_rename)) }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogVisible = false }) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }
}

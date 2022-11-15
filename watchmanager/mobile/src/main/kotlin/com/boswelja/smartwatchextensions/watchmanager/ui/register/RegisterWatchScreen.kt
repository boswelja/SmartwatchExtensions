package com.boswelja.smartwatchextensions.watchmanager.ui.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.watchmanager.R
import com.boswelja.smartwatchextensions.watchmanager.domain.AvailableWatch
import org.koin.androidx.compose.getViewModel

@Composable
fun RegisterWatchScreen(
    onRegistrationFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterWatchViewModel = getViewModel()
) {
    val availableWatches by viewModel.availableWatches.collectAsState()
    var registeringWatch by remember { mutableStateOf<AvailableWatch?>(null) }

    Column(modifier) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(Modifier.fillMaxSize()) {
            items(
                items = availableWatches,
                key = { it.id }
            ) { watch ->
                WatchItem(
                    watch = watch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            registeringWatch = watch
                        }
                )
            }
        }
    }

    registeringWatch?.let {
        ConfirmRegisterDialog(
            onDismissDialog = {
                registeringWatch = null
            },
            onConfirmClick = {
                viewModel.registerWatch(it)
                onRegistrationFinished()
            }
        )
    }
}

@Composable
fun ConfirmRegisterDialog(
    onDismissDialog: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissDialog,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmClick()
                    onDismissDialog()
                }
            ) {
                Text(stringResource(R.string.register_watch_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissDialog
            ) {
                Text(stringResource(R.string.register_watch_cancel))
            }
        },
        title = {
            Text(stringResource(R.string.register_watch_title))
        },
        text = {
            Text(stringResource(R.string.register_watch_description))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchItem(
    watch: AvailableWatch,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineText = {
            Text(watch.name)
        },
        leadingContent = {
            Icon(Icons.Default.Watch, null)
        },
        modifier = modifier
    )
}

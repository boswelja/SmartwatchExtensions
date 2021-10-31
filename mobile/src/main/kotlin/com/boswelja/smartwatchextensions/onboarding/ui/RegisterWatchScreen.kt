package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.watchconnection.common.Watch

/**
 * A screen to display available watches and allow users to complete the registration flow.
 * @param availableWatches The available watches to be registered.
 * @param onWatchRegistered Called when a watch was successfully registered.
 * @param registerWatch Called when a watch should be registered. The function takes a watch as a
 * parameter, and returns a boolean indicating whether the registration was successful.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterWatchScreen(
    availableWatches: List<Watch>,
    onWatchRegistered: () -> Unit,
    registerWatch: suspend (Watch) -> Boolean,
    modifier: Modifier = Modifier
) {
    var registeringWatch by remember {
        mutableStateOf<Watch?>(null)
    }
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(stringResource(R.string.onboarding_register_watch_title))
                }
            )
        }
    ) {
        AvailableWatchList(
            watches = availableWatches,
            onWatchSelected = { watch ->
                registeringWatch = watch
            },
            modifier = modifier
        )
    }
    registeringWatch?.let {
        RegisterWatchFlow(
            watch = it,
            registerWatch = registerWatch,
            onComplete = { success ->
                if (success) onWatchRegistered()
                else registeringWatch = null
            }
        )
    }
}

/**
 * Display a list of available watches.
 * @param watches The list of available watches to display.
 * @param onWatchSelected Called when a watch is selected.
 * @param modifier [Modifier].
 * @param itemModifier A [Modifier] to be applied to each displayed watch.
 * @param contentPadding The padding around the list of watches.
 * @param verticalArrangement The vertical arrangement of all displayed watches.
 */
@Composable
fun AvailableWatchList(
    watches: List<Watch>,
    onWatchSelected: (Watch) -> Unit,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(watches) { watch ->
            AvailableWatch(
                watch = watch,
                onClick = { onWatchSelected(watch) },
                modifier = itemModifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Displays info about a single watch.
 * @param watch The watch to display information for.
 * @param onClick Called when the watch is clicked.
 * @param modifier [Modifier].
 */
@Composable
fun AvailableWatch(
    watch: Watch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Watch,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(watch.name)
    }
}

/**
 * Handles the watch registration Flow. Dialogs are displayed for every step of the process to let
 * the user know what's happening.
 * @param watch The watch to register.
 * @param registerWatch Called when a watch should be registered. The function takes a watch as a
 * parameter, and returns a boolean indicating whether the registration was successful.
 * @param onComplete Called when watch registration finishes and the user has dismissed any dialogs.
 * @param modifier [Modifier].
 */
@Composable
fun RegisterWatchFlow(
    watch: Watch,
    registerWatch: suspend (Watch) -> Boolean,
    onComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var result by remember {
        mutableStateOf<Boolean?>(null)
    }

    LaunchedEffect(watch) {
        result = registerWatch(watch)
    }

    when (result) {
        null -> {
            RegisteringWatchDialog(
                watch = watch,
                modifier = modifier
            )
        }
        true -> {
            RegistrationSuccessDialog(
                watch = watch,
                onDismissRequest = { onComplete(true) },
                modifier = modifier
            )
        }
        false -> {
            RegistrationFailedDialog(
                watch = watch,
                onDismissRequest = { onComplete(false) },
                modifier = modifier
            )
        }
    }
}

/**
 * Display a dialog with information about the watch that is currently being registered.
 * @param watch The watch that is being registered.
 * @param modifier [Modifier].
 */
@Composable
fun RegisteringWatchDialog(
    watch: Watch,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { },
        confirmButton = { },
        title = {
            Text(stringResource(R.string.please_wait))
        },
        text = {
            Text(stringResource(R.string.onboarding_registering_watch, watch.name))
        }
    )
}

/**
 * Displays a dialog with information about a watch whose registration failed.
 * @param watch The watch whose registration failed.
 * @param onDismissRequest Called when the dialog should be dismissed.
 * @param modifier [Modifier].
 */
@Composable
fun RegistrationFailedDialog(
    watch: Watch,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.button_ok))
            }
        },
        icon = {
            Icon(Icons.Default.Error, null)
        },
        title = {
            Text(stringResource(R.string.onboarding_register_failed_title))
        },
        text = {
            Text(stringResource(R.string.onboarding_register_failed_desc, watch.name))
        }
    )
}

/**
 * Displays a dialog with information about a watch whose registration succeeded.
 * @param watch The watch whose registration succeeded.
 * @param onDismissRequest Called when the dialog should be dismissed.
 * @param modifier [Modifier].
 */
@Composable
fun RegistrationSuccessDialog(
    watch: Watch,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Text(stringResource(R.string.button_finish))
        },
        icon = {
            Icon(Icons.Default.CheckCircle, null)
        },
        title = {
            Text(stringResource(R.string.onboarding_register_success_title))
        },
        text = {
            Text(stringResource(R.string.onboarding_register_success_desc, watch.name))
        }
    )
}

package com.boswelja.smartwatchextensions.watchmanager.ui.info

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.BigButton
import com.boswelja.smartwatchextensions.common.ui.ExpandableCard
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WatchInfoActivity : AppCompatActivity() {

    private val watchId by lazy { intent?.getStringExtra(EXTRA_WATCH_ID)!! }
    private val viewModel: WatchInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.watchId.tryEmit(UUID.fromString(watchId))

        setContent {
            AppTheme {
                val scaffoldState = rememberScaffoldState()

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = { UpNavigationAppBar(onNavigateUp = { finish() }) }
                ) {
                    WatchInfoScreen(
                        onShowSnackbar = {
                            scaffoldState.snackbarHostState.showSnackbar(it)
                        },
                        onWatchRemoved = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

@Composable
fun WatchInfoScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onShowSnackbar: suspend (String) -> Unit,
    onWatchRemoved: () -> Unit
) {
    val viewModel: WatchInfoViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val watchName by viewModel.watchName.collectAsState(null, Dispatchers.IO)
    val capabilities by viewModel.getCapabilities()
        .collectAsState(emptyList(), Dispatchers.IO)

    Column(
        modifier.padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        Icon(Icons.Outlined.Watch, null, Modifier.size(180.dp))
        WatchNameField(
            watchName = watchName ?: "",
            onWatchNameChanged = { newName ->
                scope.launch {
                    viewModel.updateWatchName(newName)
                }
            }
        )
        WatchActions(
            watchName = watchName ?: "",
            onResetSettings = {
                scope.launch {
                    viewModel.resetWatchPreferences()
                    onShowSnackbar(context.getString(R.string.clear_preferences_success))
                }
            },
            onForgetWatch = {
                scope.launch {
                    viewModel.forgetWatch()
                    onWatchRemoved()
                }
            }
        )
        WatchCapabilityCard(capabilities = capabilities)
    }
}

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
        label = { Text(stringResource(R.string.watch_name_field_hint)) },
        isError = !nameValid,
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

@Composable
fun WatchCapabilityCard(
    modifier: Modifier = Modifier,
    capabilities: List<Capability>
) {
    var capabilitiesExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    ExpandableCard(
        modifier = modifier,
        title = { Text(stringResource(R.string.capabilities_title)) },
        expanded = capabilitiesExpanded,
        toggleExpanded = { capabilitiesExpanded = !capabilitiesExpanded }
    ) {
        Column {
            capabilities.forEach { capability ->
                Text(stringResource(capability.label))
            }
        }
    }
}

@Composable
fun WatchActions(
    modifier: Modifier = Modifier,
    contentSpacing: Dp = 1.dp,
    watchName: String,
    onResetSettings: () -> Unit,
    onForgetWatch: () -> Unit
) {
    var clearSettingsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var forgetWatchDialogVisible by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(contentSpacing)) {
            BigButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Outlined.ClearAll, null) },
                text = { Text(stringResource(R.string.clear_preferences_button_text)) },
                onClick = { clearSettingsDialogVisible = true }
            )
            BigButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Outlined.Delete, null) },
                text = { Text(stringResource(R.string.button_forget_watch)) },
                onClick = { forgetWatchDialogVisible = true }
            )
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
            onDismissDialog = { forgetWatchDialogVisible = false },
            onForgetWatch = onForgetWatch
        )
    }
}

@Composable
fun ForgetWatchDialog(
    modifier: Modifier = Modifier,
    watchName: String,
    onDismissDialog: () -> Unit,
    onForgetWatch: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissDialog,
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
            TextButton(onClick = onDismissDialog) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

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

package com.boswelja.smartwatchextensions.watchinfo.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class WatchInfoActivity : AppCompatActivity() {

    private val watchId by lazy { intent?.getStringExtra(EXTRA_WATCH_ID)!! }
    private val viewModel: WatchInfoViewModel by viewModels()
    private var watchName by mutableStateOf("")

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setWatch(UUID.fromString(watchId))
        viewModel.watch.observe(this) {
            it?.let { watch -> watchName = watch.name }
        }

        setContent {
            AppTheme {
                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()

                var watchNameError by remember { mutableStateOf(false) }
                var clearPreferencesDialogVisible by remember { mutableStateOf(false) }
                var forgetWatchDialogVisible by remember { mutableStateOf(false) }
                val capabilities by viewModel.getCapabilities().observeAsState()
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = { UpNavigationAppBar(onNavigateUp = { finish() }) }
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Watch, null, Modifier.size(180.dp))
                        TextField(
                            value = watchName,
                            label = { Text(stringResource(R.string.watch_name_field_hint)) },
                            isError = watchNameError,
                            onValueChange = {
                                watchNameError = it.isBlank()
                                watchName = it
                                Timber.d("Watch name changed to $it")
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            stringResource(R.string.capabilities_title),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        capabilities?.let { capabilities ->
                            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                                items(capabilities) { capability ->
                                    Text(
                                        stringResource(capability.label),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.body1
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = { clearPreferencesDialogVisible = true },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(Icons.Outlined.ClearAll, null)
                            Text(stringResource(R.string.clear_preferences_button_text))
                        }
                        OutlinedButton(
                            onClick = { forgetWatchDialogVisible = true },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(Icons.Outlined.Delete, null)
                            Text(stringResource(R.string.button_forget_watch))
                        }
                    }
                    if (clearPreferencesDialogVisible) {
                        ResetSettingsDialog(
                            onDismissDialog = {
                                clearPreferencesDialogVisible = false
                                if (it) {
                                    scope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(
                                            getString(R.string.clear_preferences_success)
                                        )
                                    }
                                }
                            }
                        )
                    }
                    if (forgetWatchDialogVisible) {
                        ForgetWatchDialog(
                            onDismissDialog = {
                                forgetWatchDialogVisible = false
                                if (it) finish()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (watchName.isNotBlank()) {
            viewModel.updateWatchName(watchName)
        }
    }

    @Composable
    fun ResetSettingsDialog(onDismissDialog: (Boolean) -> Unit) {
        AlertDialog(
            onDismissRequest = { onDismissDialog(false) },
            title = { Text(stringResource(R.string.clear_preferences_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.clear_preferences_dialog_message,
                        viewModel.watch.value?.name.toString()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetWatchPreferences()
                        onDismissDialog(true)
                    }
                ) {
                    Text(stringResource(R.string.dialog_button_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissDialog(false) }) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }

    @Composable
    fun ForgetWatchDialog(onDismissDialog: (Boolean) -> Unit) {
        AlertDialog(
            onDismissRequest = { onDismissDialog(false) },
            title = { Text(stringResource(R.string.forget_watch_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.forget_watch_dialog_message,
                        viewModel.watch.value?.name.toString(),
                        viewModel.watch.value?.name.toString()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.forgetWatch()
                        onDismissDialog(true)
                    }
                ) {
                    Text(stringResource(R.string.button_forget_watch))
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissDialog(false) }) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

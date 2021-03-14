package com.boswelja.devicemanager.watchinfo.ui

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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.rememberScaffoldState
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
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.connection.Capability
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchInfoActivity : AppCompatActivity() {

    private val forgetWatchSheet by lazy { ForgetWatchSheet() }
    private val clearPreferencesSheet by lazy { ClearWatchPreferencesSheet() }
    private val watchId by lazy { intent?.getStringExtra(EXTRA_WATCH_ID)!! }
    private val viewModel: WatchInfoViewModel by viewModels()
    private var watchName by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setWatch(watchId)
        viewModel.watch.observe(this) {
            watchName = it.name
        }

        setContent {
            AppTheme {
                val scaffoldState = rememberScaffoldState()
                var watchNameError by remember { mutableStateOf(false) }
                val watch by viewModel.watch.observeAsState()
                val scope = rememberCoroutineScope()
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
                        if (watch?.capabilities != null && watch!!.capabilities != 0.toShort()) {
                            Text(
                                stringResource(R.string.capabilities_title),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            val capabilities = Capability.values()
                                .filter { watch!!.hasCapability(it) }
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
                            onClick = {
                                viewModel.refreshCapabilities()
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        getString(R.string.refresh_capabilities_requested)
                                    )
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(Icons.Outlined.Refresh, null)
                            Text(stringResource(R.string.refresh_capabilities))
                        }
                        OutlinedButton(
                            onClick = {
                                clearPreferencesSheet.show(
                                    supportFragmentManager,
                                    ClearWatchPreferencesSheet::class.simpleName
                                )
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(Icons.Outlined.ClearAll, null)
                            Text(stringResource(R.string.clear_preferences_button_text))
                        }
                        OutlinedButton(
                            onClick = {
                                forgetWatchSheet.show(
                                    supportFragmentManager,
                                    ForgetWatchSheet::class.simpleName
                                )
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(Icons.Outlined.Delete, null)
                            Text(stringResource(R.string.button_forget_watch))
                        }
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

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

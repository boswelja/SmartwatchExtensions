package com.boswelja.devicemanager.dndsync.ui.helper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import timber.log.Timber

class DnDSyncHelperActivity : BaseToolbarActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate() called")

        setContent {
            val viewModel: HelperViewModel = viewModel()
            val setupResult by viewModel.result.observeAsState()
            var isLoading by mutableStateOf(false)
            MaterialTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(onNavigateUp = { finish() })
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            icon = {
                                if (setupResult != null) {
                                    Icon(Icons.Outlined.Check, null)
                                } else {
                                    Icon(Icons.Outlined.NavigateNext, null)
                                }
                            },
                            text = {
                                if (setupResult != null) {
                                    Text(stringResource(R.string.button_finish))
                                } else {
                                    Text(stringResource(R.string.button_next))
                                }
                            },
                            onClick = {
                                if (setupResult != null) {
                                    finish()
                                } else if (!isLoading) {
                                    isLoading = true
                                    viewModel.requestCheckPermission()
                                }
                            }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) {
                    if (setupResult != null) {
                        ResultScreen(setupResult == true)
                    } else {
                        SetupScreen(isLoading = isLoading)
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun SetupScreen(isLoading: Boolean) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.dnd_sync_helper_setup_title),
                style = MaterialTheme.typography.h4
            )
            if (isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            SetupInstructions()
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun SetupInstructions() {
        val setupInstructions = stringArrayResource(R.array.interrupt_filter_sync_to_watch_steps)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(setupInstructions) { instruction ->
                ListItem(
                    text = {
                        Text(instruction)
                    }
                )
            }
        }
    }

    @Composable
    fun ResultScreen(wasSuccessful: Boolean) {
        val icon: ImageVector
        val title: String
        val desc: String
        if (wasSuccessful) {
            icon = Icons.Outlined.Check
            title = stringResource(R.string.dnd_sync_helper_success_title)
            desc = stringResource(R.string.dnd_sync_helper_success_message)
        } else {
            icon = Icons.Outlined.ErrorOutline
            title = stringResource(R.string.dnd_sync_helper_failed_title)
            desc = stringResource(R.string.dnd_sync_helper_failed_message)
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                Modifier.size(180.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.h4
            )
            Text(
                desc,
                style = MaterialTheme.typography.h5
            )
        }
    }
}

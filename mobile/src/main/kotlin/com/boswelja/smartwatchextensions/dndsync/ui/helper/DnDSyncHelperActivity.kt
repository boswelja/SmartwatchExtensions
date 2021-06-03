package com.boswelja.smartwatchextensions.dndsync.ui.helper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

class DnDSyncHelperActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate() called")

        setContent {
            val viewModel: HelperViewModel = viewModel()
            val setupResult by viewModel.result.observeAsState()
            var isLoading by remember { mutableStateOf(false) }
            AppTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(
                            title = {
                                if (setupResult == null) {
                                    Text(stringResource(R.string.dnd_sync_helper_setup_title))
                                }
                            },
                            onNavigateUp = { finish() }
                        )
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
                        SetupScreen(
                            isLoading = isLoading,
                            modifier = Modifier.padding(it)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun SetupScreen(
        isLoading: Boolean,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
        ) {
            if (isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            SetupInstructions()
        }
    }

    @Composable
    fun SetupInstructions() {
        val setupInstructions = stringArrayResource(R.array.interrupt_filter_sync_to_watch_steps)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 72.dp, top = 8.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(setupInstructions) { index, instruction ->
                Row {
                    Text(
                        text = stringResource(id = R.string.setup_step_format, index + 1),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    SelectionContainer {
                        Text(
                            text = instruction
                        )
                    }
                }
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

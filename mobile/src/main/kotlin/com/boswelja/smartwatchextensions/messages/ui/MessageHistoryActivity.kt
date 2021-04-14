package com.boswelja.smartwatchextensions.messages.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.smartwatchextensions.messages.Message
import kotlinx.coroutines.launch

class MessageHistoryActivity : AppCompatActivity() {

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val viewModel: MessageHistoryViewModel = viewModel()
                val messages by viewModel.dismissedMessagesFlow.collectAsState(emptyList())
                val scope = rememberCoroutineScope()
                val scaffoldState = rememberScaffoldState()
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationAppBar(
                            title = { Text(stringResource(R.string.message_history_label)) },
                            actions = {
                                val clearText = if (messages.isEmpty())
                                    stringResource(R.string.message_history_not_cleared)
                                else
                                    stringResource(R.string.message_history_cleared)
                                IconButton({
                                    scope.launch {
                                        viewModel.clearMessageHistory()
                                        scaffoldState.snackbarHostState.showSnackbar(clearText)
                                    }
                                }) {
                                    Icon(Icons.Outlined.ClearAll, null)
                                }
                            },
                            onNavigateUp = { finish() }
                        )
                    }
                ) {
                    Crossfade(targetState = messages.isNotEmpty()) {
                        if (it) MessageList(messages = messages)
                        else NoMessagesView()
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun MessageList(messages: List<Message>) {
        LazyColumn {
            items(messages) { message ->
                MessageItem(message, showAction = false)
            }
        }
    }

    @Composable
    fun NoMessagesView() {
        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.message_history_empty),
                style = MaterialTheme.typography.h6
            )
        }
    }
}

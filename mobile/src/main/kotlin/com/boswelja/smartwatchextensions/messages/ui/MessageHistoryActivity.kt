package com.boswelja.smartwatchextensions.messages.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
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
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.smartwatchextensions.messages.Message
import kotlinx.coroutines.launch

class MessageHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val scaffoldState = rememberScaffoldState()
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationAppBar(
                            title = { Text(stringResource(R.string.message_history_label)) },
                            onNavigateUp = { finish() }
                        )
                    }
                ) {
                    MessageHistoryScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        onShowSnackbar = { scaffoldState.snackbarHostState.showSnackbar(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun MessageHistoryScreen(
    modifier: Modifier = Modifier,
    onShowSnackbar: suspend (String) -> Unit
) {
    val viewModel: MessageHistoryViewModel = viewModel()
    val messages by viewModel.dismissedMessagesFlow.collectAsState(emptyList())
    val scope = rememberCoroutineScope()

    Crossfade(
        targetState = messages.isNotEmpty()
    ) {
        if (it) {
            val historyClearedText = stringResource(R.string.message_history_cleared)
            MessagesHistoryList(
                modifier = modifier,
                messages = messages,
                onClearAll = {
                    scope.launch {
                        viewModel.clearMessageHistory()
                        onShowSnackbar(historyClearedText)
                    }
                }
            )
        } else NoMessageHistory(modifier)
    }
}

@Composable
fun NoMessageHistory(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.message_history_empty),
            style = MaterialTheme.typography.h6
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessagesHistoryList(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    onClearAll: () -> Unit
) {
    Card(modifier) {
        LazyColumn {
            item {
                ListItem(
                    modifier = Modifier.clickable(onClick = onClearAll),
                    text = { Text(stringResource(R.string.message_history_clear_all)) },
                    icon = { Icon(Icons.Outlined.ClearAll, null) }
                )
                Divider()
            }
            items(messages) { message ->
                MessageItem(message, showAction = false)
            }
        }
    }
}

package com.boswelja.smartwatchextensions.messages.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.messages.Message
import kotlinx.coroutines.launch

@Composable
fun MessageHistoryScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onShowSnackbar: suspend (String) -> Unit
) {
    val viewModel: MessageHistoryViewModel = viewModel()
    val messages by viewModel.dismissedMessagesFlow.collectAsState(emptyList())
    val scope = rememberCoroutineScope()

    Crossfade(
        modifier = modifier,
        targetState = messages.isNotEmpty()
    ) {
        if (it) {
            val historyClearedText = stringResource(R.string.message_history_cleared)
            MessagesHistoryList(
                modifier = Modifier.padding(contentPadding),
                messages = messages,
                onClearAll = {
                    scope.launch {
                        viewModel.clearMessageHistory()
                        onShowSnackbar(historyClearedText)
                    }
                }
            )
        } else NoMessageHistory(Modifier.fillMaxSize().padding(contentPadding))
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MessagesHistoryList(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    onClearAll: () -> Unit
) {
    Card(modifier) {
        LazyColumn {
            stickyHeader {
                ListItem(
                    modifier = Modifier
                        .clickable(onClick = onClearAll),
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

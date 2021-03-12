package com.boswelja.devicemanager.messages.ui

import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.messages.Message
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

class MessageHistoryActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
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
                if (messages.isNotEmpty()) {
                    MessageList(messages = messages)
                } else {
                    NoMessagesView()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_history_menu, menu)
        return true
    }

    @Composable
    fun MessageItem(message: Message) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                painterResource(id = message.icon.iconRes),
                null,
                Modifier.size(24.dp)
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    message.title,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    message.text,
                    style = MaterialTheme.typography.body2
                )
            }
            Text(
                getReceivedString(message.timestamp),
                style = MaterialTheme.typography.body2
            )
        }
    }

    @Composable
    fun MessageList(messages: List<Message>) {
        LazyColumn {
            items(messages) { message ->
                MessageItem(message)
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

    /**
     * Convert millisecond time into a readable date string.
     * Shows time received if it's within the last 24 hours, otherwise shows the date.
     * @param timeInMillis The time milliseconds to convert to a readable string.
     */
    private fun getReceivedString(timeInMillis: Long): String {
        val todayMillis = System.currentTimeMillis()
        val received = Date(timeInMillis)
        val isToday = (todayMillis - timeInMillis) < TimeUnit.DAYS.toMillis(1)
        return if (isToday) {
            DateFormat.getTimeFormat(this).format(received)
        } else {
            DateFormat.getDateFormat(this).format(received)
        }
    }
}

package com.boswelja.smartwatchextensions.messages.ui

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.AnimatedVisibilityItem
import com.boswelja.smartwatchextensions.common.ui.SwipeDismissItem
import com.boswelja.smartwatchextensions.messages.Message
import kotlinx.coroutines.launch

@Composable
fun MessagesScreen(scaffoldState: ScaffoldState) {
    val context = LocalContext.current
    val viewModel: MessagesViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val messages by viewModel.activeMessagesFlow.collectAsState(emptyList())

    Crossfade(targetState = messages.isNotEmpty()) {
        if (it) {
            MessagesList(
                messages = messages,
                onMessageDismissed = { message ->
                    scope.launch {
                        viewModel.dismissMessage(message.id)
                        val result = scaffoldState.snackbarHostState.showSnackbar(
                            context.getString(R.string.message_dismissed),
                            context.getString(R.string.button_undo),
                            SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.restoreMessage(message.id)
                        }
                    }
                },
                onMessageActionClicked = { action ->
                    when (action) {
                        Message.Action.LAUNCH_NOTIFICATION_SETTINGS -> {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .apply {
                                    putExtra(
                                        Settings.EXTRA_APP_PACKAGE,
                                        context.packageName
                                    )
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            context.startActivity(intent)
                        }
                        Message.Action.LAUNCH_CHANGELOG -> {
                            viewModel.customTabsIntent.launchUrl(
                                context,
                                context.getString(R.string.changelog_url).toUri()
                            )
                        }
                        Message.Action.INSTALL_UPDATE ->
                            viewModel.startUpdateFlow(context as Activity)
                    }
                }
            )
        } else {
            NoMessagesView()
        }
    }
}

@Composable
fun MessagesList(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    onMessageDismissed: (Message) -> Unit,
    onMessageActionClicked: (Message.Action) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(messages) { message ->
            var isRemoving by remember { mutableStateOf(false) }
            var isDismissing by remember { mutableStateOf(false) }
            AnimatedVisibilityItem(
                remove = isRemoving,
                item = message,
                onItemRemoved = onMessageDismissed
            ) {
                SwipeDismissItem(
                    item = message,
                    icon = Icons.Outlined.Archive,
                    onItemDismissed = { isRemoving = true },
                    onDismissingChanged = { isDismissing = it }
                ) {
                    Card(
                        elevation = animateDpAsState(
                            if (isDismissing) 4.dp else 1.dp
                        ).value
                    ) {
                        MessageItem(
                            message,
                            onActionClick = onMessageActionClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoMessagesView() {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.DoneAll,
            contentDescription = null,
            Modifier.size(120.dp)
        )
        Text(
            stringResource(R.string.messages_empty),
            style = MaterialTheme.typography.h5
        )
        OutlinedButton(
            modifier = Modifier.padding(top = 16.dp),
            onClick = {
                context.startActivity<MessageHistoryActivity>()
            }
        ) {
            Text(stringResource(R.string.message_history_label))
        }
    }
}

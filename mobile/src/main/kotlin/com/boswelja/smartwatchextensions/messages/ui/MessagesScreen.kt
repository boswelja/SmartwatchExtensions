package com.boswelja.smartwatchextensions.messages.ui

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.SnackbarDuration
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.AnimatedVisibilityItem
import com.boswelja.smartwatchextensions.common.ui.SwipeDismissItem
import com.boswelja.smartwatchextensions.messages.DisplayMessage
import com.boswelja.smartwatchextensions.messages.Message
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying messages.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onShowSnackbar Called when a snackbar should be displayed.
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onShowSnackbar: suspend (String, SnackbarDuration) -> Unit,
    onNavigateTo: (MessageDestination) -> Unit
) {
    val context = LocalContext.current
    val viewModel: MessagesViewModel = getViewModel()
    val scope = rememberCoroutineScope()
    val messages by viewModel.activeMessagesFlow.collectAsState(emptyList())

    Crossfade(targetState = messages.isNotEmpty()) {
        if (it) {
            MessagesList(
                modifier = modifier,
                contentPadding = contentPadding,
                messages = messages,
                onMessageDismissed = { message ->
                    scope.launch {
                        viewModel.dismissMessage(message.id)
                        onShowSnackbar(
                            context.getString(R.string.message_dismissed),
                            SnackbarDuration.Long
                        )
                    }
                },
                onMessageActionClicked = { action ->
                    when (action) {
                        Message.Action.NOTIFICATION_SETTINGS -> {
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
                        Message.Action.CHANGELOG -> {
                            context.startActivity { intent ->
                                intent.action = Intent.ACTION_VIEW
                                intent.data = context.getString(R.string.changelog_url).toUri()
                                intent
                            }
                        }
                        Message.Action.INSTALL_UPDATE ->
                            viewModel.startUpdateFlow(context as Activity)
                        Message.Action.NONE -> { } // Do nothing
                    }
                }
            )
        } else {
            NoMessagesView(
                modifier = modifier.padding(contentPadding),
                onNavigateTo = onNavigateTo
            )
        }
    }
}

/**
 * A Composable for displaying a list of messages.
 * @param modifier [Modifier].
 * @param contentPadding The list padding.
 * @param messages The list of messages.
 * @param onMessageDismissed Called when a message is dismissed.
 * @param onMessageActionClicked Called when a message action is clicked.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessagesList(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    messages: List<DisplayMessage>,
    onMessageDismissed: (DisplayMessage) -> Unit,
    onMessageActionClicked: (Message.Action) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(contentPadding),
        contentPadding = PaddingValues(contentPadding)
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
                    backgroundColor = MaterialTheme.colors.onBackground.copy(alpha = 0.12f),
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

/**
 * A Composable to indicate there are currently no messages.
 * @param modifier [Modifier].
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun NoMessagesView(
    modifier: Modifier = Modifier,
    onNavigateTo: (MessageDestination) -> Unit,
) {
    Column(
        modifier,
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
            onClick = { onNavigateTo(MessageDestination.MessageHistory) }
        ) {
            Text(stringResource(R.string.message_history_label))
        }
    }
}

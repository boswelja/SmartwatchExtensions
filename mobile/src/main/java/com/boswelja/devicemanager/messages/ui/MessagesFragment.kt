package com.boswelja.devicemanager.messages.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.rememberDismissState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.messages.Message
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

class MessagesFragment : Fragment() {

    private val customTabsIntent: CustomTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel: MessagesViewModel = viewModel()
                val scaffoldState = rememberScaffoldState()
                val messages by viewModel.activeMessagesFlow.collectAsState(emptyList())
                Scaffold(scaffoldState = scaffoldState) {
                    if (messages.isNotEmpty()) {
                        MessagesList(messages, scaffoldState)
                    } else {
                        NoMessagesView()
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun MessagesList(messages: List<Message>, scaffoldState: ScaffoldState) {
        val viewModel: MessagesViewModel = viewModel()
        val scope = rememberCoroutineScope()
        LazyColumn {
            items(messages) { message ->
                val dismissState = rememberDismissState {
                    if (it != DismissValue.Default) {
                        scope.launch {
                            viewModel.dismissMessage(message.id)
                            val result = scaffoldState.snackbarHostState.showSnackbar(
                                getString(R.string.message_dismissed),
                                getString(R.string.button_undo),
                                SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreMessage(message.id)
                            }
                        }
                        true
                    } else false
                }
                SwipeToDismiss(
                    state = dismissState,
                    background = {
                        val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                        val color = Color.LightGray
                        val alignment = when (direction) {
                            DismissDirection.StartToEnd -> Alignment.CenterStart
                            DismissDirection.EndToStart -> Alignment.CenterEnd
                        }
                        val icon = Icons.Outlined.Archive

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 32.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                ) {
                    Card(
                        elevation = animateDpAsState(
                            if (dismissState.dismissDirection != null) 4.dp else 0.dp
                        ).value
                    ) {
                        MessageItem(message)
                    }
                }
            }
        }
    }

    @Composable
    fun NoMessagesView() {
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
                    findNavController()
                        .navigate(MessagesFragmentDirections.toMessageHistoryActivity())
                }
            ) {
                Text(stringResource(R.string.message_history_label))
            }
        }
    }

    @Composable
    fun MessageItem(message: Message) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth()
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
            if (message.action != null) {
                val viewModel: MessagesViewModel = viewModel()
                OutlinedButton(
                    onClick = {
                        when (message.action) {
                            Message.Action.LAUNCH_NOTIFICATION_SETTINGS -> openNotiSettings()
                            Message.Action.LAUNCH_CHANGELOG -> showChangelog()
                            Message.Action.INSTALL_UPDATE ->
                                viewModel.startUpdateFlow(requireActivity())
                        }
                    },
                    content = { Text(stringResource(message.action.labelRes)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }

    /**
     * Open Android's notification settings for Wearable Extensions.
     */
    private fun openNotiSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            }
        } else {
            Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                putExtra("app_package", requireContext().packageName)
                putExtra("app_uid", requireContext().applicationInfo.uid)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun showChangelog() {
        customTabsIntent.launchUrl(requireContext(), getString(R.string.changelog_url).toUri())
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
            DateFormat.getTimeFormat(requireContext()).format(received)
        } else {
            DateFormat.getDateFormat(requireContext()).format(received)
        }
    }
}

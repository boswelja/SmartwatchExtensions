package com.boswelja.smartwatchextensions.messages.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.messages.DisplayMessage
import com.boswelja.smartwatchextensions.messages.Message
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Convert millisecond time into a readable date string.
 * Shows time received if it's within the last 24 hours, otherwise shows the date.
 * @param timeInMillis The time milliseconds to convert to a readable string.
 */
@Composable
private fun getReceivedString(timeInMillis: Long): String {
    val todayMillis = System.currentTimeMillis()
    val received = Date(timeInMillis)
    val isToday = (todayMillis - timeInMillis) < TimeUnit.DAYS.toMillis(1)
    return if (isToday) {
        DateFormat.getTimeFormat(LocalContext.current).format(received)
    } else {
        DateFormat.getDateFormat(LocalContext.current).format(received)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessageItem(
    message: DisplayMessage,
    showAction: Boolean = true,
    onActionClick: (Message.Action) -> Unit = { }
) {
    val icon = remember(message) {
        when (message.icon) {
            Message.Icon.ERROR -> Icons.Outlined.ErrorOutline
            Message.Icon.UPDATE -> Icons.Outlined.Update
            Message.Icon.HELP -> Icons.Outlined.HelpOutline
        }
    }
    val actionLabelRes = remember(message) {
        when (message.action) {
            Message.Action.NONE -> null
            Message.Action.NOTIFICATION_SETTINGS -> R.string.message_action_noti_settings
            Message.Action.CHANGELOG -> R.string.message_action_changelog
            Message.Action.INSTALL_UPDATE -> R.string.message_action_install_update
        }
    }
    ListItem(
        icon = {
            Icon(
                icon,
                null,
                Modifier.size(40.dp)
            )
        },
        text = {
            Text(
                message.title,
                style = MaterialTheme.typography.body1
            )
        },
        secondaryText = {
            Column {
                Text(
                    message.text,
                    style = MaterialTheme.typography.body2
                )
                if (showAction && actionLabelRes != null) {
                    OutlinedButton(
                        onClick = { onActionClick(message.action) },
                        content = { Text(stringResource(actionLabelRes)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        },
        trailing = {
            Text(
                getReceivedString(message.timestamp),
                style = MaterialTheme.typography.body2
            )
        }
    )
}

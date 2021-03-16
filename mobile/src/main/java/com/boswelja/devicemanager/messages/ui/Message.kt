package com.boswelja.devicemanager.messages.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.boswelja.devicemanager.messages.Message
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

@ExperimentalMaterialApi
@Composable
fun MessageItem(message: Message) {
    ListItem(
        icon = {
            Icon(
                painterResource(id = message.icon.iconRes),
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
            Text(
                message.text,
                style = MaterialTheme.typography.body2
            )
        },
        trailing = {
            Text(
                getReceivedString(message.timestamp),
                style = MaterialTheme.typography.body2
            )
        }
    )
}

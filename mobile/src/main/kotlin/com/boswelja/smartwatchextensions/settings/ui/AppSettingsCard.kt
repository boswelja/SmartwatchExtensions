package com.boswelja.smartwatchextensions.settings.ui

import android.content.Intent
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.EXTRA_APP_PACKAGE
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader

/**
 * A Composable for displaying app settings.
 * @param modifier [Modifier].
 */
@Composable
fun AppSettingsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        header = {
            CardHeader(title = { Text(stringResource(R.string.app_settings_title)) })
        }
    ) {
        Row(
            modifier = Modifier.clickable {
                Intent()
                    .apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        action = ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(EXTRA_APP_PACKAGE, context.packageName!!)
                    }
                    .also {
                        context.startActivity(it)
                    }
            }
        ) {
            Icon(Icons.Outlined.Notifications, null)
            Text(stringResource(R.string.noti_settings_title))
        }
    }
}

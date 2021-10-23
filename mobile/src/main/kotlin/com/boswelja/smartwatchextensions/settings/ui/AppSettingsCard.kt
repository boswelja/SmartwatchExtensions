package com.boswelja.smartwatchextensions.settings.ui

import android.content.Intent
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.EXTRA_APP_PACKAGE
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.common.ui.CheckboxSetting
import com.boswelja.smartwatchextensions.managespace.ui.ManageSpaceActivity
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppSettingsCard(modifier: Modifier = Modifier) {
    val viewModel: AppSettingsViewModel = getViewModel()
    val context = LocalContext.current
    val checkUpdatesDaily by viewModel.checkUpdatesDaily.collectAsState(false, Dispatchers.IO)
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState(false, Dispatchers.IO)

    Card(
        modifier = modifier,
        header = {
            CardHeader(title = { Text(stringResource(R.string.app_settings_title)) })
        }
    ) {
        Column {
            ListItem(
                text = { Text(stringResource(R.string.noti_settings_title)) },
                icon = {
                    Icon(Icons.Outlined.Notifications, null)
                },
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
            )
            ListItem(
                text = { Text(stringResource(R.string.manage_space_title)) },
                icon = { Icon(Icons.Outlined.Storage, null) },
                modifier = Modifier.clickable {
                    context.startActivity<ManageSpaceActivity>()
                }
            )
            CheckboxSetting(
                label = { Text(stringResource(R.string.check_updates_daily_title)) },
                icon = { Icon(Icons.Outlined.Update, null) },
                checked = checkUpdatesDaily,
                onCheckChanged = viewModel::setCheckUpdatesDaily
            )
            CheckboxSetting(
                label = { Text(stringResource(R.string.analytics_enabled_title)) },
                icon = { Icon(Icons.Outlined.Analytics, null) },
                checked = analyticsEnabled,
                onCheckChanged = {
                    viewModel.setAnalyticsEnabled(it)
                }
            )
        }
    }
}

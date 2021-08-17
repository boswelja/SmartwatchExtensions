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
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.common.ui.CheckboxSetting
import com.boswelja.smartwatchextensions.common.ui.DialogSetting
import com.boswelja.smartwatchextensions.managespace.ui.ManageSpaceActivity
import com.boswelja.smartwatchextensions.settings.Settings
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppSettingsCard(modifier: Modifier = Modifier) {
    val viewModel: AppSettingsViewModel = viewModel()
    val context = LocalContext.current
    val currentAppTheme by viewModel.appTheme
        .collectAsState(Settings.Theme.FOLLOW_SYSTEM, Dispatchers.IO)
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
            DialogSetting(
                label = { Text(stringResource(R.string.app_theme_title)) },
                summary = { Text(getLabelForTheme(currentAppTheme)) },
                icon = {
                    val icon = if (currentAppTheme == Settings.Theme.DARK)
                        Icons.Outlined.DarkMode
                    else
                        Icons.Outlined.LightMode
                    Icon(icon, null)
                },
                values = Settings.Theme.values().toList(),
                value = currentAppTheme,
                onValueChanged = { viewModel.setAppTheme(it) },
                valueLabel = { Text(getLabelForTheme(it)) }
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

@Composable
private fun getLabelForTheme(theme: Settings.Theme): String {
    return when (theme) {
        Settings.Theme.LIGHT -> stringResource(R.string.app_theme_light)
        Settings.Theme.DARK -> stringResource(R.string.app_theme_dark)
        Settings.Theme.FOLLOW_SYSTEM -> stringResource(R.string.app_theme_follow_system)
    }
}

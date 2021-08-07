package com.boswelja.smartwatchextensions.appsettings.ui

import android.content.Intent
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.EXTRA_APP_PACKAGE
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Divider
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
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appsettings.Settings
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.CheckboxSetting
import com.boswelja.smartwatchextensions.common.ui.DialogSetting
import com.boswelja.smartwatchextensions.common.ui.HeaderItem
import com.boswelja.smartwatchextensions.managespace.ui.ManageSpaceActivity
import com.boswelja.smartwatchextensions.watchmanager.ui.WatchManagerActivity
import com.boswelja.smartwatchextensions.widget.ui.WidgetSettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun AppSettingsScreen() {
    Column(Modifier.fillMaxSize()) {
        AppSettings()
        Divider()
        AnalyticsSettings()
        Divider()
        QSTileSettings()
        Divider()
        WatchSettings()
    }
}

@ExperimentalMaterialApi
@Composable
fun AppSettings() {
    Column {
        val viewModel: AppSettingsViewModel = viewModel()
        val context = LocalContext.current
        val currentAppTheme by viewModel.appTheme
            .collectAsState(Settings.Theme.FOLLOW_SYSTEM, Dispatchers.IO)
        val checkUpdatesDaily by viewModel.checkUpdatesDaily.collectAsState(false, Dispatchers.IO)

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
        ListItem(
            text = { Text(stringResource(R.string.widget_settings_title)) },
            icon = { Icon(Icons.Outlined.Widgets, null) },
            modifier = Modifier.clickable {
                context.startActivity<WidgetSettingsActivity>()
            }
        )
        DialogSetting(
            text = stringResource(R.string.app_theme_title),
            secondaryText = when (currentAppTheme) {
                Settings.Theme.LIGHT -> stringResource(R.string.app_theme_light)
                Settings.Theme.DARK -> stringResource(R.string.app_theme_dark)
                Settings.Theme.FOLLOW_SYSTEM -> stringResource(R.string.app_theme_follow_system)
            },
            icon = if (currentAppTheme == Settings.Theme.DARK)
                Icons.Outlined.DarkMode
            else
                Icons.Outlined.LightMode,
            values = Settings.Theme.values().toList(),
            value = currentAppTheme,
            onValueChanged = { viewModel.setAppTheme(it) },
            valueLabel = {
                val text = when (it) {
                    Settings.Theme.LIGHT -> stringResource(R.string.app_theme_light)
                    Settings.Theme.DARK -> stringResource(R.string.app_theme_dark)
                    Settings.Theme.FOLLOW_SYSTEM -> stringResource(R.string.app_theme_follow_system)
                }
                Text(text)
            }
        )
        CheckboxSetting(
            text = stringResource(R.string.check_updates_daily_title),
            icon = Icons.Outlined.Update,
            isChecked = checkUpdatesDaily,
            onCheckChanged = viewModel::setCheckUpdatesDaily
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun AnalyticsSettings() {
    val viewModel: AppSettingsViewModel = viewModel()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState(false, Dispatchers.IO)
    Column {
        HeaderItem(stringResource(R.string.category_analytics))
        CheckboxSetting(
            text = stringResource(R.string.analytics_enabled_title),
            icon = Icons.Outlined.Analytics,
            isChecked = analyticsEnabled,
            onCheckChanged = {
                viewModel.setAnalyticsEnabled(it)
            }
        )
    }
}

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun QSTileSettings(
    modifier: Modifier = Modifier
) {
    val viewModel: AppSettingsViewModel = viewModel()
    val registeredWatches by viewModel.registeredWatches.collectAsState(emptyList(), Dispatchers.IO)
    val qsTilesWatch by viewModel.qsTilesWatch.collectAsState(null, Dispatchers.IO)
    Column(modifier) {
        HeaderItem(stringResource(R.string.category_qstiles))
        DialogSetting(
            icon = Icons.Outlined.Watch,
            text = stringResource(R.string.qstiles_selected_watch),
            secondaryText = qsTilesWatch?.name,
            values = registeredWatches,
            value = qsTilesWatch,
            onValueChanged = {
                viewModel.setQSTilesWatch(it!!)
            },
            valueLabel = {
                Text(it?.name ?: stringResource(R.string.watch_status_error))
            }
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun WatchSettings() {
    val context = LocalContext.current
    Column {
        HeaderItem(stringResource(R.string.category_watch_settings))
        ListItem(
            text = { Text(stringResource(R.string.manage_watches_title)) },
            icon = { Icon(Icons.Outlined.Watch, null) },
            modifier = Modifier.clickable {
                context.startActivity<WatchManagerActivity>()
            }
        )
    }
}

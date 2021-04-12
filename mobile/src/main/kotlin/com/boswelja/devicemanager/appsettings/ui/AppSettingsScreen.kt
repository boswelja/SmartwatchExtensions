package com.boswelja.devicemanager.appsettings.ui

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
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appsettings.Settings
import com.boswelja.devicemanager.common.ui.CheckboxPreference
import com.boswelja.devicemanager.common.ui.DialogPreference
import com.boswelja.devicemanager.common.ui.HeaderItem
import com.boswelja.devicemanager.managespace.ui.ManageSpaceActivity
import com.boswelja.devicemanager.watchmanager.ui.WatchManagerActivity
import com.boswelja.devicemanager.widget.ui.WidgetSettingsActivity

@ExperimentalMaterialApi
@Composable
fun AppSettingsScreen() {
    Column(Modifier.fillMaxSize()) {
        AppSettings()
        Divider()
        AnalyticsSettings()
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
        val appThemeOptions = remember {
            arrayOf(
                Pair(context.getString(R.string.app_theme_light), Settings.Theme.LIGHT),
                Pair(context.getString(R.string.app_theme_dark), Settings.Theme.DARK),
                Pair(
                    context.getString(R.string.app_theme_follow_system),
                    Settings.Theme.FOLLOW_SYSTEM
                )
            )
        }
        val currentAppTheme by viewModel.appTheme.observeAsState()
        val currentThemeOption = appThemeOptions.first {
            it.second == (currentAppTheme ?: Settings.Theme.FOLLOW_SYSTEM)
        }

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
                context.startActivity(Intent(context, ManageSpaceActivity::class.java))
            }
        )
        ListItem(
            text = { Text(stringResource(R.string.widget_settings_title)) },
            icon = { Icon(Icons.Outlined.Widgets, null) },
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, WidgetSettingsActivity::class.java))
            }
        )
        DialogPreference(
            text = stringResource(R.string.app_theme_title),
            secondaryText = currentThemeOption.first,
            icon = if (currentAppTheme == Settings.Theme.DARK)
                Icons.Outlined.DarkMode
            else
                Icons.Outlined.LightMode,
            values = appThemeOptions,
            value = currentThemeOption,
            onValueChanged = { viewModel.setAppTheme(it.second) }
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun AnalyticsSettings() {
    val viewModel: AppSettingsViewModel = viewModel()
    val analyticsEnabled by viewModel.analyticsEnabled.observeAsState()
    Column {
        HeaderItem(stringResource(R.string.category_analytics))
        CheckboxPreference(
            text = stringResource(R.string.analytics_enabled_title),
            icon = Icons.Outlined.Analytics,
            isChecked = analyticsEnabled == true,
            onCheckChanged = {
                viewModel.setAnalyticsEnabled(it)
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
                context.startActivity(Intent(context, WatchManagerActivity::class.java))
            }
        )
    }
}

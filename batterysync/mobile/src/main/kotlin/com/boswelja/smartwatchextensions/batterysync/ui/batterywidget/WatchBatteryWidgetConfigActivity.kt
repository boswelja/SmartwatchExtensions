package com.boswelja.smartwatchextensions.batterysync.ui.batterywidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import com.boswelja.smartwatchextensions.core.ui.theme.HarmonizedTheme
import com.google.android.glance.appwidget.configuration.AppWidgetConfigurationScaffold
import com.google.android.glance.appwidget.configuration.rememberAppWidgetConfigurationState
import kotlinx.coroutines.launch

class WatchBatteryWidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HarmonizedTheme {
                ConfigurationScreen()
            }
        }
    }
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen() {
    val scope = rememberCoroutineScope()
    val configurationState = rememberAppWidgetConfigurationState(configurationInstance = WatchBatteryWidget)

    // If we don't have a valid id, discard configuration and finish the activity.
    if (configurationState.glanceId == null) {
        configurationState.discardConfiguration()
        return
    }

    AppWidgetConfigurationScaffold(
        appWidgetConfigurationState = configurationState,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        configurationState.applyConfiguration()
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Save changes")
            }
        }
    ) {

    }
}
package com.boswelja.smartwatchextensions.batterysync.ui.batterywidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.core.ui.settings.CheckboxSetting
import com.boswelja.smartwatchextensions.core.ui.settings.DialogSetting
import com.boswelja.smartwatchextensions.core.ui.theme.HarmonizedTheme
import com.google.android.glance.appwidget.configuration.AppWidgetConfigurationScaffold
import com.google.android.glance.appwidget.configuration.rememberAppWidgetConfigurationState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject

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
    val scrollState = rememberScrollState()

    // If we don't have a valid id, discard configuration and finish the activity.
    if (configurationState.glanceId == null) {
        configurationState.discardConfiguration()
        return
    }

    val watchRepository: WatchRepository by inject()
    val registeredWatches by watchRepository.registeredWatches.collectAsState(initial = emptyList())
    val watchId = configurationState.getCurrentState<Preferences>()?.get(WatchBatteryWidget.watchIdKey)
    val watch = remember(watchId, registeredWatches) { registeredWatches.firstOrNull { it.uid == watchId }}

    AppWidgetConfigurationScaffold(
        appWidgetConfigurationState = configurationState,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        if (!watchId.isNullOrEmpty()) {
                            configurationState.applyConfiguration()
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Save changes")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
        ) {
            DialogSetting(
                label = { Text("Select a Watch") },
                values = registeredWatches,
                value = watch,
                onValueChanged = { newValue ->
                    configurationState.updateCurrentState<Preferences> {
                        it.toMutablePreferences().apply {
                            if (newValue == null) {
                                remove(WatchBatteryWidget.watchIdKey)
                                remove(WatchBatteryWidget.watchNameKey)
                            } else {
                                set(WatchBatteryWidget.watchIdKey, newValue.uid)
                                set(WatchBatteryWidget.watchNameKey, newValue.name)
                            }
                        }
                    }
                }
            ) {
                Text(it?.name ?: "No watch selected")
            }
            CheckboxSetting(
                checked = configurationState.getCurrentState<Preferences>()?.get(WatchBatteryWidget.showNameKey) ?: false,
                onCheckedChange = { newValue ->
                    configurationState.updateCurrentState<Preferences> {
                        it.toMutablePreferences().apply {
                            set(WatchBatteryWidget.showNameKey, newValue)
                        }
                    }
                },
                text = {
                    Text(text = "Show Watch Name")
                }
            )
        }
    }
}

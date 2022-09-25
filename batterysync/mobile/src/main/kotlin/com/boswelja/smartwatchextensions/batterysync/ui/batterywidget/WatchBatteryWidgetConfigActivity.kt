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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.core.ui.settings.CheckboxSetting
import com.boswelja.smartwatchextensions.core.ui.settings.DialogSetting
import com.boswelja.smartwatchextensions.core.ui.theme.HarmonizedTheme
import com.google.android.glance.appwidget.configuration.AppWidgetConfigurationScaffold
import com.google.android.glance.appwidget.configuration.rememberAppWidgetConfigurationState
import kotlinx.coroutines.flow.first
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
    val snackbarHostState = remember { SnackbarHostState() }

    // If we don't have a valid id, discard configuration and finish the activity.
    if (configurationState.glanceId == null) {
        configurationState.discardConfiguration()
        return
    }

    val watchRepository: WatchRepository by inject()
    val batteryStatsRepository: BatteryStatsRepository by inject()
    val registeredWatches by watchRepository.registeredWatches.collectAsState(initial = emptyList())
    val watchId = configurationState.getCurrentState<Preferences>()?.get(WatchBatteryWidget.watchIdKey)
    val watch = remember(watchId, registeredWatches) { registeredWatches.firstOrNull { it.uid == watchId }}
    val showWatchName = configurationState.getCurrentState<Preferences>()?.get(WatchBatteryWidget.showNameKey) ?: true

    LaunchedEffect(watchId) {
        if (watchId != null) {
            val batteryStats = batteryStatsRepository.getBatteryStatsForWatch(watchId).first()
            if (batteryStats != null) {
                configurationState.updateCurrentState<Preferences> {
                    it.toMutablePreferences().apply {
                        set(WatchBatteryWidget.batteryPercentKey, batteryStats.percent)
                    }
                }
                return@LaunchedEffect
            }
        }
        // If we make it this far, remove state
//        configurationState.updateCurrentState<Preferences> {
//            it.toMutablePreferences().apply {
//                removeIfExists(WatchBatteryWidget.batteryPercentKey)
//            }
//        }
    }

    AppWidgetConfigurationScaffold(
        appWidgetConfigurationState = configurationState,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        if (!watchId.isNullOrEmpty()) {
                            configurationState.applyConfiguration()
                        } else {
                            snackbarHostState.showSnackbar(
                                message = "Please select a watch for this widget"
                            )
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
                                removeIfExists(WatchBatteryWidget.watchIdKey)
                                removeIfExists(WatchBatteryWidget.watchNameKey)
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
                checked = showWatchName,
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

internal fun MutablePreferences.removeIfExists(key: Preferences.Key<*>) {
    if (contains(key)) remove(key)
}

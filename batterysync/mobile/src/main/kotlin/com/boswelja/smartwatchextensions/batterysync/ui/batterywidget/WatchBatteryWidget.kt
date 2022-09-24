package com.boswelja.smartwatchextensions.batterysync.ui.batterywidget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.dynamicThemeColorProviders
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text

object WatchBatteryWidget : GlanceAppWidget() {

    val showNameKey = booleanPreferencesKey("show-name")
    val watchIdKey = stringPreferencesKey("watch-id")
    val watchNameKey = stringPreferencesKey("watch-name")
    val batteryPercentKey = intPreferencesKey("battery-percent")

    override val sizeMode: SizeMode = SizeMode.Exact

    @Composable
    override fun Content() {
        val watchId = currentState(key = watchIdKey)

        val colors = dynamicThemeColorProviders()
        val contentModifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(colors.background)
            .cornerRadius(16.dp)
            .padding(8.dp)
        if (watchId != null) {
            BatteryStatsContent(contentModifier)
        } else {
            NoWatchContent(contentModifier)
        }
    }

    @Composable
    private fun NoWatchContent(
        modifier: GlanceModifier = GlanceModifier
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No watch selected")
        }
    }
    
    @Composable
    fun BatteryStatsContent(
        modifier: GlanceModifier = GlanceModifier
    ) {
        val showName = currentState(key = showNameKey) ?: true
        val batteryPercent = currentState(key = batteryPercentKey) ?: -1
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showName) {
                Text(currentState(key = watchNameKey) ?: "")
            }
            Text(batteryPercent.toString())
        }
    }
}

class WatchBatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WatchBatteryWidget
}

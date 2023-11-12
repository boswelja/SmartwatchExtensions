package com.boswelja.smartwatchextensions.batterysync.ui.batterywidget

import android.content.Context
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.getBatteryDrawableRes

object WatchBatteryWidget : GlanceAppWidget() {

    val showNameKey = booleanPreferencesKey("show-name")
    val watchIdKey = stringPreferencesKey("watch-id")
    val watchNameKey = stringPreferencesKey("watch-name")
    val batteryPercentKey = intPreferencesKey("battery-percent")

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val watchId = currentState(key = watchIdKey)
            GlanceTheme {
                val contentModifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(GlanceTheme.colors.surface)
                    .cornerRadius(16.dp)
                    .padding(8.dp)
                if (watchId != null) {
                    BatteryStatsContent(contentModifier)
                } else {
                    NoWatchContent(contentModifier)
                }
            }
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

        val batteryPercentText = if (batteryPercent > -1) {
            LocalContext.current.getString(R.string.widget_battery_percent, batteryPercent.toString())
        } else {
            LocalContext.current.getString(R.string.widget_battery_unavailable)
        }
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showName) {
                Text(currentState(key = watchNameKey) ?: "")
                Spacer(GlanceModifier.height(8.dp))
            }
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.primaryContainer)
                    .cornerRadius(16.dp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    provider = ImageProvider(getBatteryDrawableRes(batteryPercent)),
                    contentDescription = null,
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = batteryPercentText,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }
}

class WatchBatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WatchBatteryWidget
}

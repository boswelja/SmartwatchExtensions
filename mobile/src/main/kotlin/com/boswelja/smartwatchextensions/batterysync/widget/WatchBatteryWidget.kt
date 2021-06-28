package com.boswelja.smartwatchextensions.batterysync.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncSettingsActivity
import com.boswelja.smartwatchextensions.common.WatchWidgetProvider
import com.boswelja.smartwatchextensions.common.getBatteryDrawable
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.flow.firstOrNull

class WatchBatteryWidget : WatchWidgetProvider() {

    override suspend fun onUpdateView(
        context: Context,
        width: Int,
        height: Int,
        watch: Watch
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_watch_battery)

        // Set click intent
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, BatterySyncSettingsActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        ).also {
            remoteViews.setOnClickPendingIntent(R.id.widget_background, it)
        }

        val isBatterySyncEnabled = WatchSettingsDatabase.getInstance(context)
            .boolSettings()
            .get(watch.id, BATTERY_SYNC_ENABLED_KEY)
            .firstOrNull()
            ?.value ?: false
        if (isBatterySyncEnabled) {
            val batteryPercent = WatchBatteryStatsDatabase.getInstance(context)
                .batteryStatsDao().getStats(watch.id).firstOrNull()?.percent ?: -1

            // Set battery indicator image
            remoteViews.setImageViewResource(
                R.id.battery_indicator, getBatteryDrawable(batteryPercent)
            )

            // Set battery indicator text
            if (batteryPercent >= 0) {
                remoteViews.setTextViewText(
                    R.id.battery_indicator_text,
                    context.getString(
                        R.string.battery_percent, batteryPercent.toString()
                    )
                )
            } else {
                remoteViews.setTextViewText(
                    R.id.battery_indicator_text, context.getString(R.string.battery_sync_disabled)
                )
            }
        } else {
            remoteViews.setImageViewResource(
                R.id.battery_indicator, R.drawable.battery_unknown
            )
            remoteViews.setTextViewText(
                R.id.battery_indicator_text, context.getString(R.string.battery_sync_disabled)
            )
        }

        return remoteViews
    }
}

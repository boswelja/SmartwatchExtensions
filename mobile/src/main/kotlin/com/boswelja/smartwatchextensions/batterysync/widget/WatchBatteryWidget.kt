package com.boswelja.smartwatchextensions.batterysync.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.common.ui.BaseWidgetProvider
import com.boswelja.smartwatchextensions.main.MainActivity
import java.util.UUID
import kotlinx.coroutines.flow.firstOrNull

class WatchBatteryWidget : BaseWidgetProvider() {

    override suspend fun onUpdateView(
        context: Context,
        width: Int,
        height: Int,
        watchId: UUID
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_watch_battery)

        // Set click intent
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        ).also {
            remoteViews.setOnClickPendingIntent(R.id.widget_background, it)
        }

        val batteryPercent = WatchBatteryStatsDatabase.getInstance(context)
            .batteryStatsDao().getStats(watchId).firstOrNull()?.percent ?: -1

        // Set battery indicator image
        ContextCompat.getDrawable(context, R.drawable.ic_watch_battery)!!
            .apply {
                level = batteryPercent
            }
            .also { indicator ->
                remoteViews.setImageViewBitmap(R.id.battery_indicator, indicator.toBitmap())
            }

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

        return remoteViews
    }
}

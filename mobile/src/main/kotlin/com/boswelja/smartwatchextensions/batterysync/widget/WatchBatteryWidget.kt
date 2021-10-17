package com.boswelja.smartwatchextensions.batterysync.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepositoryLoader
import com.boswelja.smartwatchextensions.batterysync.common.getBatteryDrawableRes
import com.boswelja.smartwatchextensions.common.WatchWidgetProvider
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.kodein.di.DIAware
import org.kodein.di.LateInitDI
import org.kodein.di.instance

class WatchBatteryWidget : WatchWidgetProvider(), DIAware {

    override val di = LateInitDI()

    private val settingsRepository: WatchSettingsRepository by instance()

    override suspend fun onUpdateView(
        context: Context,
        width: Int,
        height: Int,
        watch: Watch
    ): RemoteViews {
        di.baseDI = (context.applicationContext as DIAware).di

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_watch_battery)

        remoteViews.setTextViewText(R.id.watch_name, watch.name)

        // Set click intent
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        ).also {
            remoteViews.setOnClickPendingIntent(R.id.widget_background, it)
        }

        val isBatterySyncEnabled = settingsRepository
            .getBoolean(watch.uid, BATTERY_SYNC_ENABLED_KEY, false).first()
        if (isBatterySyncEnabled) {
            val batteryPercent = BatteryStatsRepositoryLoader.getInstance(context)
                .batteryStatsFor(watch.uid).firstOrNull()?.percent ?: -1

            // Set battery indicator image
            remoteViews.setImageViewResource(
                R.id.battery_indicator, getBatteryDrawableRes(batteryPercent)
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

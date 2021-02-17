/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appsettings.ui.AppSettingsFragment.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.devicemanager.appsettings.ui.AppSettingsFragment.Companion.WIDGET_BACKGROUND_OPACITY_KEY
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.main.MainActivity
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WatchBatteryWidget : AppWidgetProvider() {

    private val coroutineScope = MainScope()

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                WidgetDatabase.getInstance(context!!).also {
                    for (widgetId in appWidgetIds) {
                        it.removeWidget(widgetId)
                    }
                    it.close()
                }
            }
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            for (widgetId in appWidgetIds) {
                val options = appWidgetManager?.getAppWidgetOptions(widgetId)!!
                val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
                updateView(context, appWidgetManager, widgetId, width, height)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        if (newOptions != null && (
            newOptions.containsKey(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) ||
                newOptions.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            )
        ) {
            val height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            val width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            updateView(context, appWidgetManager, appWidgetId, width, height)
        }
    }

    /**
     * Update a specified widget's view with new data.
     * @param context [Context].
     * @param appWidgetManager The [AppWidgetManager] instance to send the updated view to.
     * @param appWidgetId The ID of the widget to update.
     * @param width The target width of the updated view.
     * @param height The target height of the updated view.
     */
    private fun updateView(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        width: Int,
        height: Int
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val widgetDatabase = WidgetDatabase.getInstance(context!!)
            val batteryStatsDatabase = WatchBatteryStatsDatabase.getInstance(context)

            val watchId =
                widgetDatabase.getForWidget(appWidgetId)?.watchId
            if (!watchId.isNullOrEmpty()) {
                val batteryStats = batteryStatsDatabase.batteryStatsDao().getStatsForWatch(watchId)
                val remoteViews = createWidgetRemoteView(context, width, height, batteryStats)
                appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
            }

            widgetDatabase.close()
        }
    }

    /**
     * Create a [RemoteViews] object representing a widget.
     * @param context [Context].
     * @param width The target width of the updated view.
     * @param height The target height of the updated view.
     * @param batteryStats The [WatchBatteryStats] object containing battery information for the
     * widget to use, if available.
     */
    private fun createWidgetRemoteView(
        context: Context?,
        width: Int,
        height: Int,
        batteryStats: WatchBatteryStats?
    ): RemoteViews {
        val batteryPercent = batteryStats?.percent ?: 0

        val remoteViews = RemoteViews(context?.packageName, R.layout.widget_watch_battery)

        // Set click intent
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        ).also {
            remoteViews.setOnClickPendingIntent(R.id.widget_background, it)
        }

        if (width > 0 && height > 0) {
            // Set widget background
            PreferenceManager.getDefaultSharedPreferences(context).also { sharedPreferences ->
                if (sharedPreferences.getBoolean(SHOW_WIDGET_BACKGROUND_KEY, true)) {
                    val opacity = sharedPreferences.getInt(WIDGET_BACKGROUND_OPACITY_KEY, 60)
                    val calculatedAlpha = ((opacity / 100.0f) * 255).toInt()
                    context?.let {
                        ContextCompat.getDrawable(context, R.drawable.widget_background)!!
                            .apply { alpha = calculatedAlpha }
                            .also { widgetBackground ->
                                remoteViews.setImageViewBitmap(
                                    R.id.widget_background,
                                    widgetBackground.toBitmap(width, height)
                                )
                            }
                    }
                } else {
                    remoteViews.setInt(R.id.widget_background, "setBackgroundColor", 0)
                }
            }
        }

        context?.let {
            // Set battery indicator image
            ContextCompat.getDrawable(context, R.drawable.ic_watch_battery)!!
                .apply {
                    level = batteryPercent
                    setTint(ContextCompat.getColor(it, R.color.widgetForeground))
                }
                .also { remoteViews.setImageViewBitmap(R.id.battery_indicator, it.toBitmap()) }

            // Set battery indicator text
            if (batteryPercent >= 0) {
                remoteViews.setTextViewText(
                    R.id.battery_indicator_text,
                    context.getString(
                        R.string.battery_sync_percent_short, batteryPercent.toString()
                    )
                )
            } else {
                remoteViews.setTextViewText(
                    R.id.battery_indicator_text, context.getString(R.string.battery_sync_disabled)
                )
            }
        }

        return remoteViews
    }

    companion object {

        /**
         * Update all [WatchBatteryWidget] instances.
         * @param context [Context].
         */
        fun updateWidgets(context: Context) {
            val ids =
                AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context, WatchBatteryWidget::class.java))
            updateWidgets(context, ids)
        }

        /**
         * Update a specified set of widgets.
         * @param context [Context].
         * @param widgetIds An array of IDs of all the [WatchBatteryWidget] instances to update.
         */
        fun updateWidgets(context: Context, widgetIds: IntArray) {
            val intent =
                Intent(context, WatchBatteryWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
            context.sendBroadcast(intent)
        }

        /**
         * Enable the [WatchBatteryWidget] component.
         * @param context [Context].
         */
        fun enableWidget(context: Context) {
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, WatchBatteryWidget::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}

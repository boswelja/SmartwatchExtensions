/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
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
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.Helper
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.ui.main.MainActivity
import com.boswelja.devicemanager.ui.main.appsettings.AppSettingsFragment.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.devicemanager.ui.main.appsettings.AppSettingsFragment.Companion.WIDGET_BACKGROUND_OPACITY_KEY
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchBatteryWidget : AppWidgetProvider() {

    private val mainScope = MainScope()

    private var widgetDatabase: WidgetDatabase? = null
    private var batteryStatsDatabase: WatchBatteryStatsDatabase? = null

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        mainScope.launch {
            widgetDatabase = WidgetDatabase.open(context!!)
            batteryStatsDatabase = Helper.openDatabase(context)
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        if (widgetDatabase != null) {
            widgetDatabase!!.close()
        }
        if (batteryStatsDatabase != null) {
            batteryStatsDatabase!!.close()
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            for (widgetId in appWidgetIds) {
                widgetDatabase?.watchBatteryWidgetDao()?.removeWidget(widgetId)
            }
        }
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            for (widgetId in appWidgetIds) {
                val options = appWidgetManager?.getAppWidgetOptions(widgetId)!!
                val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
                updateView(context, appWidgetManager, widgetId, width, height)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int, newOptions: Bundle?) {
        if (newOptions != null &&
                (newOptions.containsKey(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) ||
                newOptions.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH))) {
            val height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            val width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            updateView(context, appWidgetManager, appWidgetId, width, height)
        }
    }

    private fun updateView(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int, width: Int, height: Int) {
        mainScope.launch {
            withContext(Dispatchers.IO) {
                val watchId = widgetDatabase?.watchBatteryWidgetDao()?.findByWidgetId(appWidgetId)?.watchId
                if (!watchId.isNullOrEmpty()) {
                    val batteryStats = batteryStatsDatabase?.batteryStatsDao()?.getStatsForWatch(watchId)
                    val batterySyncEnabled = batteryStats != null
                    val percent = batteryStats?.batteryPercent ?: 0
                    val remoteViews = createView(context, width, height, batterySyncEnabled, percent)
                    appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
                }
            }
        }
    }

    companion object {

        fun updateWidgets(context: Context) {
            val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context, WatchBatteryWidget::class.java))
            updateWidgets(context, ids)
        }

        fun updateWidgets(context: Context, widgetIds: IntArray) {
            val intent = Intent(context, WatchBatteryWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            }
            context.sendBroadcast(intent)
        }

        fun createView(context: Context?, width: Int, height: Int, batterySyncEnabled: Boolean, batteryPercent: Int = 0): RemoteViews {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val remoteViews = RemoteViews(context?.packageName, R.layout.widget_watch_battery)

            PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0).also {
                remoteViews.setOnClickPendingIntent(R.id.widget_background, it)
            }

            if (sharedPrefs.getBoolean(SHOW_WIDGET_BACKGROUND_KEY, true)) {
                val opacity = sharedPrefs.getInt(WIDGET_BACKGROUND_OPACITY_KEY, 60)
                val calculatedAlpha = ((opacity / 100.0f) * 255).toInt()
                context?.getDrawable(R.drawable.widget_background)!!.apply {
                    alpha = calculatedAlpha
                }.also {
                    remoteViews.setImageViewBitmap(R.id.widget_background, it.toBitmap(width, height))
                }
            } else {
                remoteViews.setInt(R.id.widget_background, "setBackgroundColor", 0)
            }

            context?.getDrawable(R.drawable.ic_watch_battery)!!.apply {
                level = batteryPercent
            }.also {
                remoteViews.setImageViewBitmap(R.id.battery_indicator, it.toBitmap())
            }

            if (batterySyncEnabled) {
                remoteViews.setTextViewText(R.id.battery_indicator_text, context.getString(R.string.battery_sync_percent_short, batteryPercent.toString()))
            } else {
                remoteViews.setTextViewText(R.id.battery_indicator_text, context.getString(R.string.battery_sync_disabled))
            }

            return remoteViews
        }
    }
}

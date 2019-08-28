/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.main.MainActivity

class WatchBatteryWidget : AppWidgetProvider() {

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
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val percent = sharedPrefs.getInt(PreferenceKey.BATTERY_PERCENT_KEY, 0)
        val batterySyncEnabled = sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
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
            Log.d("WatchBatteryWidget", "No background")
            remoteViews.setInt(R.id.widget_background, "setBackgroundColor", 0)
        }

        context?.getDrawable(R.drawable.ic_watch_battery)!!.apply {
            level = percent
        }.also {
            remoteViews.setImageViewBitmap(R.id.battery_indicator, it.toBitmap())
        }

        if (batterySyncEnabled) {
            remoteViews.setTextViewText(R.id.battery_indicator_text, context.getString(R.string.battery_sync_percent_short, percent.toString()))
        } else {
            remoteViews.setTextViewText(R.id.battery_indicator_text, context.getString(R.string.battery_sync_disabled))
        }

        appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
    }

    companion object {
        const val  SHOW_WIDGET_BACKGROUND_KEY = "show_widget_background"
        const val  WIDGET_BACKGROUND_OPACITY_KEY = "widget_background_opacity"

        fun updateWidgets(context: Context) {
            val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context, WatchBatteryWidget::class.java))
            val intent = Intent(context, WatchBatteryWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}

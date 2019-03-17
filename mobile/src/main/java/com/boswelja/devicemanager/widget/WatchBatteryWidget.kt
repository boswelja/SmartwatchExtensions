/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
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
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.MainActivity

class WatchBatteryWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        if (appWidgetIds != null && !appWidgetIds.isEmpty()) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val percent = sharedPrefs.getInt(PreferenceKey.BATTERY_PERCENT_KEY, 0)
            val batterySyncEnabled = sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
            for (widgetId in appWidgetIds) {
                val remoteViews = RemoteViews(context?.packageName, R.layout.widget_watch_battery)
                val activityIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
                remoteViews.setOnClickPendingIntent(R.id.widget_background, activityIntent)
                val battDrawable = context?.getDrawable(R.drawable.ic_watch_battery)!!
                if (batterySyncEnabled) {
                    remoteViews.setTextViewText(R.id.battery_indicator_text, context.getString(R.string.watch_battery_status, percent.toString()))
                    battDrawable.level = percent
                } else {
                    remoteViews.setTextViewText(R.id.battery_indicator_text, context.getString(R.string.battery_sync_disabled))
                    battDrawable.level = 0
                }
                remoteViews.setImageViewBitmap(R.id.battery_indicator, CommonUtils.drawableToBitmap(battDrawable))
                appWidgetManager?.updateAppWidget(widgetId, remoteViews)
            }
        }
    }

    companion object {
        fun updateWidget(context: Context) {
            val intent = Intent(context, WatchBatteryWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context, WatchBatteryWidget::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
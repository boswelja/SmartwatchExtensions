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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.main.MainActivity
import com.boswelja.devicemanager.widget.widgetIdStore
import com.boswelja.devicemanager.widget.widgetSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WatchBatteryWidget : AppWidgetProvider() {

    private val coroutineScope = MainScope()

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (context != null && appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                context.widgetIdStore.edit { widgetIds ->
                    appWidgetIds.forEach { widgetId ->
                        widgetIds.remove(stringPreferencesKey(widgetId.toString()))
                    }
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
        if (context != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val widgetIdStore = context.widgetIdStore
                val watchId = widgetIdStore.data.map {
                    it[stringPreferencesKey(appWidgetId.toString())]
                }
                watchId.collect {
                    val batteryPercent = WatchBatteryStatsDatabase.getInstance(context)
                        .batteryStatsDao().getStatsForWatch(it)?.percent ?: -1
                    val remoteViews =
                        createWidgetRemoteView(context, width, height, batteryPercent)
                    appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
                }
            }
        }
    }

    /**
     * Create a [RemoteViews] object representing a widget.
     * @param context [Context].
     * @param width The target width of the updated view.
     * @param height The target height of the updated view.
     * @param batteryPercent The battery percent to display in the widget.
     */
    private suspend fun createWidgetRemoteView(
        context: Context?,
        width: Int,
        height: Int,
        batteryPercent: Int
    ): RemoteViews {
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

        context?.let {
            if (width > 0 && height > 0) {
                // Set widget background
                val widgetSettings = it.widgetSettings.data.first()

                // Synchronous is desired here, since we're already in a suspend function
                val showBackground = widgetSettings[SHOW_WIDGET_BACKGROUND_KEY]
                if (showBackground == true) {
                    val backgroundOpacity = widgetSettings[WIDGET_BACKGROUND_OPACITY_KEY] ?: 60
                    val calculatedAlpha = ((backgroundOpacity / 100.0f) * 255).toInt()
                    ContextCompat.getDrawable(context, R.drawable.widget_background)!!
                        .apply { alpha = calculatedAlpha }
                        .also { widgetBackground ->
                            remoteViews.setImageViewBitmap(
                                R.id.widget_background,
                                widgetBackground.toBitmap(width, height)
                            )
                        }
                } else {
                    remoteViews.setInt(R.id.widget_background, "setBackgroundColor", 0)
                }
            }

            // Set battery indicator image
            ContextCompat.getDrawable(context, R.drawable.ic_watch_battery)!!
                .apply {
                    level = batteryPercent
                    setTint(ContextCompat.getColor(it, R.color.widgetForeground))
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
        }

        return remoteViews
    }

    companion object {
        val SHOW_WIDGET_BACKGROUND_KEY = booleanPreferencesKey("show_widget_background")
        val WIDGET_BACKGROUND_OPACITY_KEY = intPreferencesKey("widget_background_opacity")

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

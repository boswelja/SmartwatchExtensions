package com.boswelja.smartwatchextensions.common.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.widget.WatchBatteryWidget
import com.boswelja.smartwatchextensions.widget.ui.WidgetSettingsActivity.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.smartwatchextensions.widget.ui.WidgetSettingsActivity.Companion.WIDGET_BACKGROUND_OPACITY_KEY
import com.boswelja.smartwatchextensions.widget.widgetIdStore
import com.boswelja.smartwatchextensions.widget.widgetSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * An [AppWidgetProvider] that applies our default widget background with user settings.
 */
abstract class BaseWidgetProvider : AppWidgetProvider() {

    internal val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Called when widget content is required. A background is not needed.
     * @param context [Context].
     * @param width The width of the widget view.
     * @param height The height of the widget.
     * @return Then widget content as a [RemoteViews].
     */
    abstract suspend fun onUpdateView(
        context: Context,
        width: Int,
        height: Int,
        watchId: String
    ): RemoteViews

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (context != null && appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            val pendingResult = goAsync()
            coroutineScope.launch(Dispatchers.IO) {
                context.widgetIdStore.edit { widgetIds ->
                    appWidgetIds.forEach { widgetId ->
                        widgetIds.remove(stringPreferencesKey(widgetId.toString()))
                    }
                }
                pendingResult.finish()
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

    private fun updateView(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        width: Int,
        height: Int
    ) {
        if (context != null) {
            val widgetView = RemoteViews(
                context.packageName,
                R.layout.common_widget_background
            )
            val pendingResult = goAsync()
            coroutineScope.launch {
                val widgetIdStore = context.widgetIdStore
                val watchId =
                    widgetIdStore.data.first()[stringPreferencesKey(appWidgetId.toString())]
                val widgetContent = if (!watchId.isNullOrBlank()) {
                    onUpdateView(context, width, height, watchId)
                } else {
                    Timber.w("Watch ID for widget %s is null or blank", appWidgetId)
                    RemoteViews(context.packageName, R.layout.common_widget_error)
                }
                widgetView.addView(R.id.widget_container, widgetContent)

                val background = getBackground(context, width, height)
                if (background != null) {
                    widgetView.setImageViewBitmap(R.id.widget_background, background)
                } else {
                    widgetView.setInt(R.id.widget_background, "setBackgroundColor", 0)
                }
                appWidgetManager?.updateAppWidget(appWidgetId, widgetView)
                pendingResult.finish()
            }
        }
    }

    private suspend fun getBackground(context: Context, width: Int, height: Int): Bitmap? {
        // Set widget background
        val widgetSettings = context.widgetSettings.data.first()

        // Synchronous is desired here, since we're already in a suspend function
        val showBackground = widgetSettings[SHOW_WIDGET_BACKGROUND_KEY]
        return if (showBackground == true) {
            val backgroundOpacity =
                widgetSettings[WIDGET_BACKGROUND_OPACITY_KEY] ?: 60
            val calculatedAlpha = ((backgroundOpacity / 100.0f) * 255).toInt()
            ContextCompat.getDrawable(context, R.drawable.widget_background)!!
                .apply { alpha = calculatedAlpha }
                .toBitmap(width, height)
        } else {
            null
        }
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
    }
}

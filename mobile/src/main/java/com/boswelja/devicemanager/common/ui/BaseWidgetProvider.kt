package com.boswelja.devicemanager.common.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.widget.ui.WidgetSettingsActivity.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.devicemanager.widget.ui.WidgetSettingsActivity.Companion.WIDGET_BACKGROUND_OPACITY_KEY
import com.boswelja.devicemanager.widget.widgetIdStore
import com.boswelja.devicemanager.widget.widgetSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    abstract fun onUpdateView(context: Context?, width: Int, height: Int): RemoteViews

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
            widgetView.addView(R.id.widget_container, onUpdateView(context, width, height))
            coroutineScope.launch {
                val background = getBackground(context, width, height)
                if (background != null) {
                    widgetView.setImageViewBitmap(R.id.widget_background, background)
                } else {
                    widgetView.setInt(R.id.widget_background, "setBackgroundColor", 0)
                }
                appWidgetManager?.updateAppWidget(appWidgetId, widgetView)
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
}

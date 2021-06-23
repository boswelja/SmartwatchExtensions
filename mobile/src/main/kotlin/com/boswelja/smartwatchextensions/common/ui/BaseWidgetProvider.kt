package com.boswelja.smartwatchextensions.common.ui

import android.app.PendingIntent
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
import com.boswelja.smartwatchextensions.main.MainActivity
import com.boswelja.smartwatchextensions.main.MainActivity.Companion.EXTRA_WATCH_ID
import com.boswelja.smartwatchextensions.widget.widgetIdStore
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * An [AppWidgetProvider] that applies our default widget background with user settings.
 */
abstract class BaseWidgetProvider : AppWidgetProvider() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    /**
     * Called when widget content is required. A background is not needed.
     * @param context [Context].
     * @param width The width of the widget view.
     * @param height The height of the widget.
     * @param watchId The ID of the watch this widget is for.
     * @return Then widget content as a [RemoteViews].
     */
    abstract suspend fun onUpdateView(
        context: Context,
        width: Int,
        height: Int,
        watchId: UUID
    ): RemoteViews

    /**
     * Create the widget click [PendingIntent].
     * @param context [Context].
     * @param watchId The ID of the watch this widget is for.
     * @return The [PendingIntent] to fire when the user clicks the widget, or null if nothing
     * should happen.
     */
    open fun onCreateClickIntent(
        context: Context,
        watchId: UUID?
    ): PendingIntent? {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            watchId?.let { putExtra(EXTRA_WATCH_ID, it.toString()) }
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        Timber.d("onDeleted called")
        if (context != null && appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            val pendingResult = goAsync()
            coroutineScope.launch {
                context.widgetIdStore.edit { widgetIds ->
                    appWidgetIds.forEach { widgetId ->
                        Timber.d("Removing widget with id = %s", widgetId)
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
        Timber.d("onUpdate called")
        if (context != null && appWidgetManager != null) {
            appWidgetIds?.forEach { widgetId ->
                Timber.i("Widget %s updated", widgetId)
                val options = appWidgetManager.getAppWidgetOptions(widgetId)
                updateView(context, widgetId, options, appWidgetManager)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        Timber.i("Widget %s options changed", appWidgetId)
        if (context != null && newOptions != null && appWidgetManager != null) {
            updateView(context, appWidgetId, newOptions, appWidgetManager)
        }
    }

    /**
     * Update the widget [RemoteViews].
     * @param context [Context].
     * @param appWidgetId The ID of the widget.
     * @param width The width of the widget.
     * @param height The height of the widget.
     * @return The widget's new [RemoteViews].
     */
    private suspend fun createRemoteViews(
        context: Context,
        appWidgetId: Int,
        width: Int,
        height: Int
    ): RemoteViews {
        // Create new RemoteViews
        val widgetView = RemoteViews(
            context.packageName,
            R.layout.common_widget_background
        )

        // Get watch ID
        val widgetIdStore = context.widgetIdStore
        val watchId = widgetIdStore.data.map { preferences ->
            preferences[stringPreferencesKey(appWidgetId.toString())]
        }.firstOrNull()?.let { value ->
            try {
                UUID.fromString(value)
            } catch (e: Exception) {
                null
            }
        }

        // Set click PendingIntent
        onCreateClickIntent(context, watchId)?.let {
            Timber.d("Setting widget click intent")
            widgetView.setOnClickPendingIntent(R.id.widget_container, it)
        }

        val widgetContent = if (watchId != null) {
            // Get the widget content from child class
            Timber.d("Getting widget content")
            onUpdateView(context, width, height, watchId)
        } else {
            Timber.w("Watch ID for widget %s is null", appWidgetId)
            // Set error view
            RemoteViews(context.packageName, R.layout.common_widget_error)
        }
        widgetView.removeAllViews(R.id.widget_container)
        widgetView.addView(R.id.widget_container, widgetContent)

        // Set background
        val background = getBackground(context, width, height)
        if (background != null) {
            widgetView.setImageViewBitmap(R.id.widget_background, background)
        } else {
            widgetView.setInt(R.id.widget_background, "setBackgroundColor", 0)
        }

        // Return new view
        return widgetView
    }

    /**
     * Get the background that should be applied to widgets.
     * @param context [Context].
     * @param width The width of the background.
     * @param height The height of the background.
     * @return The background [Bitmap], or null if there is none.
     */
    private fun getBackground(context: Context, width: Int, height: Int): Bitmap? {
        Timber.d("Getting widget background with width = %s and height = %s", width, height)
        // Set widget background

        // Synchronous is desired here, since we're already in a suspend function
        return ContextCompat.getDrawable(
            context, R.drawable.widget_background
        )?.toBitmap(width, height)
    }

    private fun updateView(
        context: Context,
        widgetId: Int,
        widgetOptions: Bundle,
        appWidgetManager: AppWidgetManager
    ) {
        // Going async
        val pendingResult = goAsync()

        // Launch coroutine
        coroutineScope.launch {
            // Get size
            val height = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            val width = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)

            // Create RemoteViews and update widget
            createRemoteViews(
                context,
                widgetId,
                width,
                height
            ).let { appWidgetManager.updateAppWidget(widgetId, it) }

            // Finish
            pendingResult.finish()
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

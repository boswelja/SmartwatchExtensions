package com.boswelja.smartwatchextensions.common.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.widget.widgetIdStore
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * An activity containing common code for widget configuration activities
 */
abstract class BaseWidgetConfigActivity : AppCompatActivity() {

    private val resultIntent = Intent()

    /**
     * The ID of the widget we're configuring
     */
    private var widgetId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the ID of the widget we're configuring
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_CANCELED, resultIntent)
    }

    /**
     * Perform finishing touches for the widget config
     * @param watch The [Watch] to configure the widget for.
     */
    internal fun finishWidgetConfig(watch: Watch) {
        Timber.d("finishWidgetConfig($watch) called")
        setResult(RESULT_OK, resultIntent)

        lifecycleScope.launch(Dispatchers.IO) {
            widgetIdStore.edit { widgetIds ->
                widgetIds[stringPreferencesKey(widgetId.toString())] = watch.id.toString()
            }
            BaseWidgetProvider.updateWidgets(
                this@BaseWidgetConfigActivity,
                intArrayOf(widgetId)
            )
            finish()
        }
    }
}

package com.boswelja.devicemanager.common.ui.activity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * An activity containing common code for widget configuration activities
 */
abstract class BaseWidgetConfigActivity : BaseToolbarActivity() {

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
            val database = WidgetDatabase.getInstance(this@BaseWidgetConfigActivity)
            database.addWidget(widgetId, watch.id)
            WatchBatteryWidget.updateWidgets(
                this@BaseWidgetConfigActivity,
                intArrayOf(widgetId)
            )
            finish()
        }
    }
}

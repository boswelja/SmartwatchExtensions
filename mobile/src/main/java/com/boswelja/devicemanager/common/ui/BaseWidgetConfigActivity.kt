package com.boswelja.devicemanager.common.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.widget.database.WatchBatteryWidgetId
import com.boswelja.devicemanager.widget.database.WatchWidgetAssociation
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
    protected var widgetId: Int = 0
    private lateinit var widgetDatabase: WidgetDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the ID of the widget we're configuring
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_CANCELED, resultIntent)
        lifecycleScope.launch(Dispatchers.IO) {
            widgetDatabase = WidgetDatabase.open(this@BaseWidgetConfigActivity)
        }
    }

    /**
     * Perform finishing touches for the widget config
     * @param widgetInfo An object containing the widget ID, and the associated watch ID.
     */
    protected fun finishWidgetConfig(widgetInfo: WatchWidgetAssociation) {
        Timber.d("finishWidgetConfig(${widgetInfo.widgetId}) called")
        setResult(RESULT_OK, resultIntent)

        lifecycleScope.launch(Dispatchers.IO) {
            when (widgetInfo) {
                is WatchBatteryWidgetId -> {
                    widgetDatabase.watchBatteryWidgetDao().addWidget(widgetInfo)
                    WatchBatteryWidget.updateWidgets(
                        this@BaseWidgetConfigActivity,
                        intArrayOf(widgetInfo.widgetId)
                    )
                }
                else -> Timber.w("Widget type not handled")
            }
            finish()
        }
    }
}

/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.widget.configuration

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.databinding.ActivityWatchBatteryWidgetConfigurationBinding
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import com.boswelja.devicemanager.widget.database.WatchBatteryWidgetId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchBatteryWidgetConfigurationActivity : BaseToolbarActivity() {

    private val resultIntent = Intent()
    private val coroutineScope = MainScope()
    private val database by lazy { WatchDatabase.get(this) }
    private val adapter by lazy {
        WatchPickerAdapter {
            finishAndCreateWidget(it.id)
        }
    }

    private lateinit var binding: ActivityWatchBatteryWidgetConfigurationBinding

    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchBatteryWidgetConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close)
        setupWatchPickerRecyclerView()

        widgetId = getWidgetId()
        resultIntent.apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_CANCELED, resultIntent)
    }

    /**
     * Gets the widget ID from the activity [getIntent].
     * @return The widget ID.
     */
    private fun getWidgetId(): Int {
        return intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    /**
     * Set up the watch picker RecyclerView.
     */
    private fun setupWatchPickerRecyclerView() {
        binding.watchPickerRecyclerView.adapter = adapter
        database.watchDao().getAllObservable().observe(this) {
            adapter.submitList(it)
        }
    }

    /**
     * Stores the new widget's information, creates it then finishes this activity.
     */
    private fun finishAndCreateWidget(watchId: String) {
        Timber.d("finishAndCreateWidget($watchId) called")
        setResult(Activity.RESULT_OK, resultIntent)

        coroutineScope.launch(Dispatchers.IO) {
            WidgetDatabase.open(this@WatchBatteryWidgetConfigurationActivity).also {
                it.watchBatteryWidgetDao().addWidget(WatchBatteryWidgetId(watchId, widgetId))
                it.close()
            }
            WatchBatteryWidget.updateWidgets(this@WatchBatteryWidgetConfigurationActivity, intArrayOf(widgetId))
            finish()
        }
    }
}

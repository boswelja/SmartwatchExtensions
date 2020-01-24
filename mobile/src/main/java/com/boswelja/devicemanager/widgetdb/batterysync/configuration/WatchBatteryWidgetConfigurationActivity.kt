/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widgetdb.batterysync.configuration

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.boswelja.devicemanager.widgetdb.batterysync.WatchBatteryWidgetId
import com.boswelja.devicemanager.widgetdb.WidgetDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchBatteryWidgetConfigurationActivity : BaseToolbarActivity() {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
            findViewById<RecyclerView>(R.id.watch_picker_recycler_view).apply {
                layoutManager = LinearLayoutManager(this@WatchBatteryWidgetConfigurationActivity, LinearLayoutManager.VERTICAL, false)
                coroutineScope.launch {
                    val watches = service.getRegisteredWatches()
                    withContext(Dispatchers.Main) {
                        adapter = WatchPickerAdapter(watches, this@WatchBatteryWidgetConfigurationActivity)
                        setLoading(false)
                    }
                }
            }
        }

        override fun onWatchManagerUnbound() {}
    }

    private lateinit var loadingSpinner: ProgressBar
    private lateinit var watchPickerRecyclerView: RecyclerView

    private var watchConnectionManager: WatchConnectionService? = null
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private val resultIntent = Intent()
    private val coroutineScope = MainScope()

    override fun getContentViewId(): Int = R.layout.activity_watch_battery_widget_configuration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadingSpinner = findViewById(R.id.loading_spinner)
        watchPickerRecyclerView = findViewById(R.id.watch_picker_recycler_view)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            title = "Watch Battery Widget Setup"
            setHomeAsUpIndicator(R.drawable.ic_close)
            setDisplayHomeAsUpEnabled(true)
        }

        widgetId = intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

        resultIntent.apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_CANCELED, resultIntent)
    }

    override fun onResume() {
        super.onResume()
        setLoading(true)

        WatchConnectionService.bind(this, watchConnectionManagerConnection)
    }

    override fun onPause() {
        super.onPause()
        unbindService(watchConnectionManagerConnection)
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loadingSpinner.visibility = View.VISIBLE
            watchPickerRecyclerView.visibility = View.GONE
        } else {
            loadingSpinner.visibility = View.GONE
            watchPickerRecyclerView.visibility = View.VISIBLE
        }
    }

    fun finishAndCreateWidget(watchId: String) {
        setResult(Activity.RESULT_OK, resultIntent)

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                WidgetDatabase.open(this@WatchBatteryWidgetConfigurationActivity).also {
                    it.watchBatteryWidgetDao().addWidget(WatchBatteryWidgetId(watchId, widgetId))
                    it.close()
                }
            }
        }

        WatchBatteryWidget.updateWidgets(this, intArrayOf(widgetId))

        finish()
    }
}

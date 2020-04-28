/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.databinding.ActivityWatchBatteryWidgetConfigurationBinding
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.widgetdb.WidgetDatabase
import com.boswelja.devicemanager.widgetdb.batterysync.WatchBatteryWidgetId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class WatchBatteryWidgetConfigurationActivity : BaseToolbarActivity() {

    private val watchManagerConnection = object : WatchManager.Connection() {
        override fun onWatchManagerBound(watchManager: WatchManager) {
            Timber.i("Watch manager bound")
            coroutineScope.launch(Dispatchers.IO) {
                val watches = watchManager.getRegisteredWatches()
                withContext(Dispatchers.Main) {
                    (binding.watchPickerRecyclerView.adapter as WatchPickerAdapter).setWatches(watches)
                    setLoading(false)
                }
            }
        }

        override fun onWatchManagerUnbound() {
            Timber.w("Watch manager unbound")
        }
    }

    private val resultIntent = Intent()
    private val coroutineScope = MainScope()

    private lateinit var binding: ActivityWatchBatteryWidgetConfigurationBinding

    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_watch_battery_widget_configuration)
        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close)
        setupWatchPickerRecyclerView()
        setLoading(true)

        widgetId = getWidgetId()
        resultIntent.apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_CANCELED, resultIntent)

        WatchManager.bind(this, watchManagerConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(watchManagerConnection)
    }

    /**
     * Gets the widget ID from the activity [getIntent].
     * @return The widget ID.
     */
    private fun getWidgetId(): Int {
        return intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    /**
     * Set up the watch picker RecyclerView.
     */
    private fun setupWatchPickerRecyclerView() {
        binding.watchPickerRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                    this@WatchBatteryWidgetConfigurationActivity,
                    LinearLayoutManager.VERTICAL,
                    false)
            adapter = WatchPickerAdapter(this@WatchBatteryWidgetConfigurationActivity)
        }
    }

    /**
     * Sets whether the UI should display a loading spinner.
     * @param isLoading true if the UI should reflect loading, false otherwise.
     */
    private fun setLoading(isLoading: Boolean) {
        Timber.d("setLoading($isLoading) called")
        binding.apply {
            if (isLoading) {
                loadingSpinner.visibility = View.VISIBLE
                watchPickerRecyclerView.visibility = View.GONE
            } else {
                loadingSpinner.visibility = View.GONE
                watchPickerRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Stores the new widget's information, creates it then finishes this activity.
     */
    fun finishAndCreateWidget(watchId: String) {
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

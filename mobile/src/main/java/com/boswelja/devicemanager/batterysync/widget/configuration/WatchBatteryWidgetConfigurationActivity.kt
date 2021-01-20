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
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.common.ui.adapter.WatchAdapter
import com.boswelja.devicemanager.databinding.ActivityWatchBatteryWidgetConfigurationBinding
import com.boswelja.devicemanager.widget.database.WatchBatteryWidgetId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchBatteryWidgetConfigurationActivity : BaseToolbarActivity() {

    private val viewModel: WatchBatteryWidgetConfigurationViewModel by viewModels()
    private val resultIntent = Intent()
    private val adapter by lazy { WatchAdapter { finishAndCreateWidget(it.id) } }

    private lateinit var binding: ActivityWatchBatteryWidgetConfigurationBinding

    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchBatteryWidgetConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close)

        binding.watchPickerRecyclerView.adapter = adapter
        viewModel.allRegisteredWatches.observe(this) {
            Timber.d("Got ${it.count()} watches")
            adapter.submitList(it)
        }

        widgetId = getWidgetId()
        resultIntent.apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId) }
        setResult(Activity.RESULT_CANCELED, resultIntent)
    }

    /**
     * Gets the widget ID from the activity [getIntent].
     * @return The widget ID.
     */
    private fun getWidgetId(): Int {
        return intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    /** Stores the new widget's information, creates it then finishes this activity. */
    private fun finishAndCreateWidget(watchId: String) {
        Timber.d("finishAndCreateWidget($watchId) called")
        setResult(Activity.RESULT_OK, resultIntent)

        viewModel.addWidgetToDatabase(WatchBatteryWidgetId(watchId, widgetId)).invokeOnCompletion {
            finish()
        }
    }
}

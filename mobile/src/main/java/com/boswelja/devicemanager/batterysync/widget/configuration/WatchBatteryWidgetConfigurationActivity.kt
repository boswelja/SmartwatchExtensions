/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.widget.configuration

import android.os.Bundle
import androidx.activity.viewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BaseWidgetConfigActivity
import com.boswelja.devicemanager.common.ui.adapter.WatchAdapter
import com.boswelja.devicemanager.databinding.ActivityWatchBatteryWidgetConfigurationBinding
import com.boswelja.devicemanager.widget.database.WatchBatteryWidgetId
import timber.log.Timber

class WatchBatteryWidgetConfigurationActivity : BaseWidgetConfigActivity() {

    private val viewModel: WatchBatteryWidgetConfigurationViewModel by viewModels()
    private val adapter by lazy {
        WatchAdapter {
            finishWidgetConfig(WatchBatteryWidgetId(it.id, widgetId))
        }
    }

    private lateinit var binding: ActivityWatchBatteryWidgetConfigurationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchBatteryWidgetConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        binding.watchPickerRecyclerView.adapter = adapter
        viewModel.allRegisteredWatches.observe(this) {
            Timber.d("Got ${it.count()} watches")
            adapter.submitList(it)
        }
    }
}

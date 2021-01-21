/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.widget.configuration

import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import androidx.activity.viewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BaseWidgetConfigActivity
import com.boswelja.devicemanager.databinding.ActivityWatchBatteryWidgetConfigurationBinding
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.widget.database.WatchBatteryWidgetId
import com.google.android.material.radiobutton.MaterialRadioButton
import timber.log.Timber

class WatchBatteryWidgetConfigurationActivity : BaseWidgetConfigActivity() {

    private val viewModel: WatchBatteryWidgetConfigurationViewModel by viewModels()

    private lateinit var binding: ActivityWatchBatteryWidgetConfigurationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the layout binding
        binding = ActivityWatchBatteryWidgetConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        // Disable the done button when nothing is selected
        binding.availableWatchesGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.finishButton.isEnabled = checkedId > -1
        }

        // Observe registered watches and keep UI populated
        viewModel.allRegisteredWatches.observe(this) {
            populateAvailableWatches(it)
        }

        // Set up finish button
        binding.finishButton.setOnClickListener {
            // Get selected watch
            val watch =
                viewModel.getWatchByIndex(binding.availableWatchesGroup.checkedRadioButtonId)
            watch?.let {
                finishWidgetConfig(WatchBatteryWidgetId(watch.id, widgetId))
            }
        }
    }

    /**
     * Populates the view with available watches to pick from
     */
    private fun populateAvailableWatches(watches: List<Watch>) {
        Timber.d("populateAvailableWatches() called")
        binding.availableWatchesGroup.apply {
            removeAllViews()
            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            watches.forEachIndexed { index, watch ->
                val radioButton = MaterialRadioButton(context)
                radioButton.id = index
                radioButton.text = watch.name
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                addView(radioButton, layoutParams)
            }
        }
    }
}

/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityWatchManagerBinding
import com.boswelja.devicemanager.onboarding.ui.OnboardingActivity
import com.boswelja.devicemanager.watchmanager.item.Watch

class WatchManagerActivity : BaseToolbarActivity() {

    private val viewModel: WatchManagerViewModel by viewModels()
    private val adapter by lazy {
        WatchManagerAdapter {
            if (it != null) openWatchInfoActivity(it) else openWatchSetupActivity()
        }
    }

    private lateinit var binding: ActivityWatchManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        binding.watchManagerRecyclerView.adapter = adapter

        viewModel.registeredWatches.observe(this) { adapter.submitList(it) }
    }

    /** Opens a [OnboardingActivity]. */
    private fun openWatchSetupActivity() {
        Intent(this, OnboardingActivity::class.java)
            .also { startActivity(it) }
    }

    /** Opens a [WatchInfoActivity]. */
    private fun openWatchInfoActivity(watch: Watch) {
        Intent(this, WatchInfoActivity::class.java)
            .apply { putExtra(WatchInfoActivity.EXTRA_WATCH_ID, watch.id) }
            .also { startActivity(it) }
    }
}

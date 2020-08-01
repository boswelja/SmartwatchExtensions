/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchmanager

import android.content.Intent
import android.os.Bundle
import com.boswelja.devicemanager.databinding.ActivityWatchManagerBinding
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.watchsetup.WatchSetupActivity
import com.boswelja.devicemanager.ui.watchsetup.WatchSetupActivity.Companion.EXTRA_SKIP_WELCOME
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch

class WatchManagerActivity :
    BaseToolbarActivity() {

    private val adapter by lazy {
        WatchManagerAdapter {
            if (it != null) openWatchInfoActivity(it)
            else openWatchSetupActivity()
        }
    }

    private lateinit var watchManager: WatchManager

    private lateinit var binding: ActivityWatchManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_WATCH_LIST_UNCHANGED)

        binding = ActivityWatchManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        binding.watchManagerRecyclerView.adapter = adapter

        watchManager = WatchManager.get(this)
        watchManager.database.watchDao().getAllObservable().observe(this) {
            adapter.submitList(it)
        }
    }

    /**
     * Opens a [WatchSetupActivity].
     */
    private fun openWatchSetupActivity() {
        Intent(this, WatchSetupActivity::class.java).apply {
            putExtra(EXTRA_SKIP_WELCOME, true)
        }.also {
            startActivityForResult(it, WATCH_SETUP_ACTIVITY_REQUEST_CODE)
        }
    }

    /**
     * Opens a [WatchInfoActivity].
     */
    private fun openWatchInfoActivity(watch: Watch) {
        Intent(this, WatchInfoActivity::class.java).apply {
            putExtra(WatchInfoActivity.EXTRA_WATCH_ID, watch.id)
        }.also {
            startActivityForResult(it, WATCH_INFO_ACTIVITY_REQUEST_CODE)
        }
    }

    companion object {
        private const val WATCH_SETUP_ACTIVITY_REQUEST_CODE = 54321
        private const val WATCH_INFO_ACTIVITY_REQUEST_CODE = 65432

        const val RESULT_WATCH_LIST_UNCHANGED = 0
    }
}

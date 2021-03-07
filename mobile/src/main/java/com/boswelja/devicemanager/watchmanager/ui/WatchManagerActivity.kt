package com.boswelja.devicemanager.watchmanager.ui

import android.content.Intent
import android.os.Bundle
import com.boswelja.devicemanager.common.LifecycleAwareTimer
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityWatchManagerBinding
import com.boswelja.devicemanager.watchinfo.ui.WatchInfoActivity
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchActivity

class WatchManagerActivity : BaseToolbarActivity() {

    private val watchManager by lazy { WatchManager.getInstance(this) }
    private val adapter by lazy {
        WatchManagerAdapter {
            if (it != null) openWatchInfoActivity(it) else openWatchSetupActivity()
        }
    }
    private val refreshDataTimer = LifecycleAwareTimer(period = 20) {
        watchManager.refreshData()
    }

    private lateinit var binding: ActivityWatchManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        binding.watchManagerRecyclerView.adapter = adapter

        watchManager.registeredWatches.observe(this) { adapter.submitList(it) }
        lifecycle.addObserver(refreshDataTimer)
    }

    /** Opens a [RegisterWatchActivity]. */
    private fun openWatchSetupActivity() {
        Intent(this, RegisterWatchActivity::class.java)
            .also { startActivity(it) }
    }

    /** Opens a [WatchInfoActivity]. */
    private fun openWatchInfoActivity(watch: Watch) {
        Intent(this, WatchInfoActivity::class.java)
            .apply { putExtra(WatchInfoActivity.EXTRA_WATCH_ID, watch.id) }
            .also { startActivity(it) }
    }
}

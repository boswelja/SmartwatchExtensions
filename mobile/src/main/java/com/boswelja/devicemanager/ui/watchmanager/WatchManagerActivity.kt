/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchmanager

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionInterface
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WatchManagerActivity :
        BaseToolbarActivity(),
        WatchConnectionInterface {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
            coroutineScope.launch {
                (watchManagerRecyclerView.adapter as WatchManagerAdapter).addWatches(service.getRegisteredWatches())
            }
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
        }
    }

    private val coroutineScope = MainScope()

    private var watchConnectionManager: WatchConnectionService? = null

    private lateinit var watchManagerRecyclerView: RecyclerView

    override fun onConnectedWatchChanged(success: Boolean) {} // Do nothing

    override fun onConnectedWatchChanging() {} // Do nothing

    override fun onWatchAdded(watch: Watch) {
        (watchManagerRecyclerView.adapter as WatchManagerAdapter).addWatch(watch)
    }

    override fun getContentViewId(): Int = R.layout.activity_watch_manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Watch Manager"
            setDisplayShowTitleEnabled(true)
        }

        setupRecyclerView()

        WatchConnectionService.bind(this, watchConnectionManagerConnection)
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(watchConnectionManagerConnection)
    }

    private fun setupRecyclerView() {
        watchManagerRecyclerView = findViewById(R.id.watch_manager_recycler_view)
        watchManagerRecyclerView.apply {
            adapter = WatchManagerAdapter()
            layoutManager = LinearLayoutManager(this@WatchManagerActivity, LinearLayoutManager.VERTICAL, false)
        }
    }
}

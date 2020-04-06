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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.watchsetup.WatchSetupActivity
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionInterface
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchManagerActivity :
        BaseToolbarActivity(),
        WatchConnectionInterface {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
            updateRegisteredWatches()
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

        setResult(RESULT_WATCH_LIST_UNCHANGED)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.watch_manager_title)
            setDisplayShowTitleEnabled(true)
        }

        setupRecyclerView()

        WatchConnectionService.bind(this, watchConnectionManagerConnection)
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(watchConnectionManagerConnection)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            WATCH_SETUP_ACTIVITY_REQUEST_CODE -> {
                when (resultCode) {
                    WatchSetupActivity.RESULT_WATCH_ADDED -> {
                        setResult(RESULT_WATCH_LIST_CHANGED)
                        updateRegisteredWatches()
                    }
                }
            }
            WATCH_INFO_ACTIVITY_REQUEST_CODE -> {
                when (resultCode) {
                    WatchInfoActivity.RESULT_WATCH_NAME_CHANGED -> {
                        setResult(RESULT_WATCH_LIST_CHANGED)
                        val watchId = data?.getStringExtra(WatchInfoActivity.EXTRA_WATCH_ID)
                        updateSingleWatch(watchId)
                    }
                    WatchInfoActivity.RESULT_WATCH_REMOVED -> {
                        setResult(RESULT_WATCH_LIST_CHANGED)
                        val watchId = data?.getStringExtra(WatchInfoActivity.EXTRA_WATCH_ID)
                        removeWatch(watchId)
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateRegisteredWatches() {
        coroutineScope.launch {
            if (watchConnectionManager != null) {
                val registeredWatches = watchConnectionManager!!.getRegisteredWatches()
                withContext(Dispatchers.Main) {
                    (watchManagerRecyclerView.adapter as WatchManagerAdapter).apply {
                        setWatches(registeredWatches)
                    }
                }
            }
        }
    }

    private fun updateSingleWatch(watchId: String?) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val newWatchInfo = watchConnectionManager?.getWatchById(watchId)
                if (newWatchInfo != null) {
                    withContext(Dispatchers.Main) {
                        (watchManagerRecyclerView.adapter as WatchManagerAdapter).updateWatch(newWatchInfo)
                    }
                }
            }
        }
    }

    private fun removeWatch(watchId: String?) {
        if (!watchId.isNullOrEmpty()) {
            (watchManagerRecyclerView.adapter as WatchManagerAdapter).removeWatch(watchId)
        }
    }

    private fun setupRecyclerView() {
        watchManagerRecyclerView = findViewById(R.id.watch_manager_recycler_view)
        watchManagerRecyclerView.apply {
            adapter = WatchManagerAdapter(this@WatchManagerActivity)
            layoutManager = LinearLayoutManager(this@WatchManagerActivity, LinearLayoutManager.VERTICAL, false)
        }
    }

    fun startWatchSetupActivity() {
        startActivityForResult(Intent(this, WatchSetupActivity::class.java), WATCH_SETUP_ACTIVITY_REQUEST_CODE)
    }

    fun startWatchInfoActivity(watch: Watch) {
        Intent(this, WatchInfoActivity::class.java).apply {
            putExtra(WatchInfoActivity.EXTRA_WATCH_ID, watch.id)
        }.also {
            startActivityForResult(it, WATCH_INFO_ACTIVITY_REQUEST_CODE)
        }
    }

    companion object {
        private const val WATCH_SETUP_ACTIVITY_REQUEST_CODE = 54321
        private const val WATCH_INFO_ACTIVITY_REQUEST_CODE = 65432

        const val RESULT_WATCH_LIST_CHANGED = 1
        const val RESULT_WATCH_LIST_UNCHANGED = 0
    }
}

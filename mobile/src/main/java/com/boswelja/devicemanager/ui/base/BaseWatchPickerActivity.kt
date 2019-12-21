/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.watchmanager.WatchManagerActivity
import com.boswelja.devicemanager.ui.watchsetup.WatchSetupActivity
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionInterface
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseWatchPickerActivity :
        BaseToolbarActivity(),
        AdapterView.OnItemSelectedListener,
        WatchConnectionInterface {

    open fun onWatchManagerBound(): Boolean {
        return true
    }

    private val watchConnManServiceConnection = object : WatchConnectionService.Connection() {

        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
            if (onWatchManagerBound()) {
                updateConnectedWatches()
            }
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
        }
    }

    private val coroutineScope = MainScope()

    var canUpdateConnectedWatches: Boolean = true
    var watchConnectionManager: WatchConnectionService? = null

    private lateinit var watchPickerSpinner: AppCompatSpinner

    override fun onItemSelected(adapterView: AdapterView<*>?, selectedView: View?, position: Int, id: Long) {
        val connectedWatchId = id.toString(36)
        watchConnectionManager?.setConnectedWatchById(connectedWatchId)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) { }

    override fun onConnectedWatchChanging() {
        watchPickerSpinner.isEnabled = false
    }

    override fun onConnectedWatchChanged(success: Boolean) {
        watchPickerSpinner.isEnabled = true
        if (!success) {
            val adapter = (watchPickerSpinner.adapter as WatchPickerAdapter)
            for (i in 0 until adapter.count) {
                val watch = adapter.getItem(i)
                if (watch?.id == watchConnectionManager?.getConnectedWatchId()) {
                    watchPickerSpinner.setSelection(i, true)
                    return
                }
            }
        }
    }

    override fun onWatchAdded(watch: Watch) {
        (watchPickerSpinner.adapter as WatchPickerAdapter).add(watch)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WatchConnectionService.bind(this, watchConnManServiceConnection)

        watchPickerSpinner = findViewById<AppCompatSpinner>(R.id.watch_picker_spinner).apply {
            onItemSelectedListener = this@BaseWatchPickerActivity
            adapter = WatchPickerAdapter(this@BaseWatchPickerActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        watchConnectionManager?.registerWatchConnectionInterface(this)
        if (watchPickerSpinner.selectedItemId.toString(36) != watchConnectionManager?.getConnectedWatchId()) {
            val adapter = (watchPickerSpinner.adapter as WatchPickerAdapter)
            for (i in 0 until adapter.count) {
                val watch = adapter.getItem(i)
                if (watch?.id == watchConnectionManager?.getConnectedWatchId()) {
                    watchPickerSpinner.setSelection(i, false)
                    return
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        watchConnectionManager?.unregisterWatchConnectionInterface(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(watchConnManServiceConnection)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            WATCH_SETUP_ACTIVITY_REQUEST_CODE -> {
                when (resultCode) {
                    WatchSetupActivity.RESULT_WATCH_ADDED -> {
                        updateConnectedWatches()
                    }
                    WatchSetupActivity.RESULT_NO_WATCH_ADDED -> {
                        finish()
                    }
                }
            }
            WATCH_MANAGER_ACTIVITY_REQUEST_CODE -> {
                when (resultCode) {
                    WatchManagerActivity.RESULT_WATCH_LIST_CHANGED -> {
                        updateConnectedWatches()
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateConnectedWatches() {
        if (canUpdateConnectedWatches) {
            coroutineScope.launch {
                withContext(Dispatchers.Default) {
                    if (watchConnectionManager != null) {
                        withContext(Dispatchers.Main) {
                            (watchPickerSpinner.adapter as WatchPickerAdapter).clear()
                        }
                        val watches = watchConnectionManager!!.getRegisteredWatches()
                        if (watches.isNotEmpty()) {
                            val connectedWatchId = watchConnectionManager!!.getConnectedWatchId()
                            var selectedWatchPosition = 0
                            watches.forEach {
                                withContext(Dispatchers.Main) {
                                    (watchPickerSpinner.adapter as WatchPickerAdapter).add(it)
                                }
                                if (it.id == connectedWatchId) {
                                    selectedWatchPosition = (watchPickerSpinner.adapter as WatchPickerAdapter).getPosition(it)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                watchPickerSpinner.setSelection(selectedWatchPosition)
                            }
                        } else {
                            startActivityForResult(Intent(this@BaseWatchPickerActivity, WatchSetupActivity::class.java), WATCH_SETUP_ACTIVITY_REQUEST_CODE)
                        }
                    }
                }
            }
        }
    }

    fun startWatchManagerActivity() {
        startActivityForResult(Intent(this, WatchManagerActivity::class.java), WATCH_MANAGER_ACTIVITY_REQUEST_CODE)
    }

    companion object {
        private const val WATCH_SETUP_ACTIVITY_REQUEST_CODE = 54321
        private const val WATCH_MANAGER_ACTIVITY_REQUEST_CODE = 65432
    }

    class WatchPickerAdapter(context: Context) : ArrayAdapter<Watch>(context, 0) {

        private val layoutInflater = LayoutInflater.from(context)

        override fun getItemId(position: Int): Long {
            return getItem(position)?.id?.toLong(36) ?: -1
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        private fun getItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            val watch = getItem(position)!!
            var view = convertView
            if (view == null) {
                view = layoutInflater.inflate(R.layout.common_spinner_item_two_line, parent, false)
            }
            view!!.findViewById<AppCompatTextView>(R.id.title).text = watch.name
            view.findViewById<AppCompatTextView>(R.id.subtitle).text = if (watch.connected) {
                if (watch.hasApp) {
                    context.getString(R.string.watch_status_connected)
                } else {
                    context.getString(R.string.watch_status_missing_app)
                }
            } else {
                context.getString(R.string.watch_status_disconnected)
            }
            return view
        }
    }
}

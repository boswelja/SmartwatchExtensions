/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionInterface
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService

abstract class BaseWatchPickerActivity :
        BaseToolbarActivity(),
        AdapterView.OnItemSelectedListener,
        WatchConnectionInterface {

    private val watchConnManServiceConnection = object : WatchConnectionService.Connection() {

        override fun onPreferenceSyncServiceBound(service: WatchConnectionService) {
            watchConnectionManager = service
        }

        override fun onPreferenceSyncServiceUnbound() {
            watchConnectionManager = null
        }
    }

    var watchConnectionManager: WatchConnectionService? = null
    var connectedWatchId: String? = null

    private lateinit var watchPickerSpinner: AppCompatSpinner

    override fun onItemSelected(adapterView: AdapterView<*>?, selectedView: View?, position: Int, id: Long) {
        connectedWatchId = id.toString(36)
        watchConnectionManager?.setConnectedWatchById(connectedWatchId!!)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) { }

    override fun onConnectedWatchChanging() {
        Log.d("BaseWatchPickerActivity", "onConnectedNodeChanging")
        watchPickerSpinner.isEnabled = false
    }

    override fun onConnectedWatchChanged() {
        Log.d("BaseWatchPickerActivity", "onConnectedNodeChanged")
        watchPickerSpinner.isEnabled = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        watchPickerSpinner = findViewById<AppCompatSpinner>(R.id.watch_picker_spinner).apply {
            onItemSelectedListener = this@BaseWatchPickerActivity
            adapter = WatchPickerAdapter(this@BaseWatchPickerActivity)
        }

        WatchConnectionService.bind(this, watchConnManServiceConnection)

        loadConnectedWatches()
    }

    override fun onResume() {
        super.onResume()
        watchConnectionManager?.registerWatchConnectionInterface(this)
    }

    override fun onPause() {
        super.onPause()
        watchConnectionManager?.unregisterWatchConnectionInterface(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(watchConnManServiceConnection)
    }

    private fun loadConnectedWatches() {
        if (watchConnectionManager != null) {
            (watchPickerSpinner.adapter as WatchPickerAdapter).clear()
            watchConnectionManager!!.getAllWatches()?.forEach {
                (watchPickerSpinner.adapter as WatchPickerAdapter).add(it)
            }
            watchPickerSpinner.setSelection((watchPickerSpinner.adapter as WatchPickerAdapter).getPosition(watchConnectionManager!!.getConnectedWatch()))
        }
    }

    class WatchPickerAdapter(context: Context, private val watches: ArrayList<Watch>) : ArrayAdapter<Watch>(context, 0) {

        private val layoutInflater = LayoutInflater.from(context)
        private val watchConnectedString = context.getString(R.string.watch_status_connected)
        private val watchMissingAppString = context.getString(R.string.watch_status_missing_app, context.getString(R.string.app_name))

        constructor(context: Context) : this(context, ArrayList<Watch>())

        override fun getCount(): Int {
            return watches.count()
        }

        override fun getItemId(position: Int): Long {
            return watches[position].id.toLong(36)
        }

        override fun addAll(collection: MutableCollection<out Watch>) {
            watches.addAll(collection)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        private fun getItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            val watch = watches[position]
            var view = convertView
            if (view == null) {
                view = layoutInflater.inflate(R.layout.common_spinner_item_two_line, parent, false)
            }
            view!!.findViewById<AppCompatTextView>(R.id.title).text = watch.name
            view.findViewById<AppCompatTextView>(R.id.subtitle).text = if (watch.hasApp) {
                watchConnectedString
            } else {
                watchMissingAppString
            }
            return view
        }

        fun add(newWatch: Watch): Int {
            watches.add(newWatch)
            return watches.indexOf(newWatch)
        }
    }
}

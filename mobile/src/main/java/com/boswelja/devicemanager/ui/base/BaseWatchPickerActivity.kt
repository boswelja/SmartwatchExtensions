/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.ui.watchmanager.WatchManagerActivity
import com.boswelja.devicemanager.ui.watchsetup.WatchSetupActivity
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionInterface
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.boswelja.devicemanager.watchconnectionmanager.WatchStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * A base activity extending [BaseToolbarActivity], additionally adding a watch picker to the
 * toolbar and reporting watch selection changes.
 */
abstract class BaseWatchPickerActivity :
        BaseToolbarActivity(),
        AdapterView.OnItemSelectedListener,
        WatchConnectionInterface {

    private val watchConnManServiceConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            Timber.i("onWatchManagerBound() called")
            watchConnectionManager = service
            updateConnectedWatches()
        }

        override fun onWatchManagerUnbound() {
            Timber.w("onWatchManagerUnbound() called")
            watchConnectionManager = null
        }
    }

    internal val coroutineScope = MainScope()

    var watchConnectionManager: WatchConnectionService? = null

    private lateinit var watchPickerSpinner: AppCompatSpinner

    override fun onItemSelected(adapterView: AdapterView<*>?, selectedView: View?, position: Int, id: Long) {
        val connectedWatchId = id.toString(36)
        Timber.i("$connectedWatchId selected")
        watchConnectionManager?.setConnectedWatchById(connectedWatchId)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) { }

    override fun onConnectedWatchChanging() {
        Timber.i("onConnectedWatchChanging() called")
        watchPickerSpinner.isEnabled = false
    }

    override fun onConnectedWatchChanged(success: Boolean) {
        Timber.i("onConnectedWatchChanged() called")
        watchPickerSpinner.isEnabled = true
        if (!success) {
            Timber.w("Failed to change selected watch")
            ensureCorrectWatchSelected()
        }
    }

    override fun onWatchAdded(watch: Watch) {
        Timber.i("onWatchAdded() called")
        (watchPickerSpinner.adapter as WatchPickerAdapter).add(watch)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")
        WatchConnectionService.bind(this, watchConnManServiceConnection)
        setupWatchPicker()
    }

    override fun onStart() {
        super.onStart()
        Timber.i("onStart() called")
        watchConnectionManager?.registerWatchConnectionInterface(this)
        ensureCorrectWatchSelected()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop() called")
        watchConnectionManager?.unregisterWatchConnectionInterface(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy() called")
        unbindService(watchConnManServiceConnection)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.i("onActivityResult() called")
        when (requestCode) {
            WATCH_SETUP_ACTIVITY_REQUEST_CODE -> {
                Timber.i("Got setup activity result")
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
                Timber.i("Got watch manager result")
                when (resultCode) {
                    WatchManagerActivity.RESULT_WATCH_LIST_CHANGED -> {
                        updateConnectedWatches()
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Checks the [watchPickerSpinner] has the correct watch selected.
     * It should always match what's selected in [WatchConnectionService].
     */
    private fun ensureCorrectWatchSelected() {
        Timber.i("ensureCorrectWatchSelected() called")
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

    /**
     * Set up [watchPickerSpinner].
     */
    private fun setupWatchPicker() {
        Timber.i("setupWatchPicker() called")
        watchPickerSpinner = findViewById<AppCompatSpinner>(R.id.watch_picker_spinner).apply {
            onItemSelectedListener = this@BaseWatchPickerActivity
            adapter = WatchPickerAdapter(this@BaseWatchPickerActivity)
        }
    }

    /**
     * Update the list of registered watches that the user can
     * choose from [watchPickerSpinner].
     */
    private fun updateConnectedWatches() {
        Timber.i("updateConnectedWatches() called")
        coroutineScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                (watchPickerSpinner.adapter as WatchPickerAdapter).clear()
            }
            val watches = watchConnectionManager?.getRegisteredWatches()
            if (watches != null) {
                if (watches.isNotEmpty()) {
                    Timber.i("Setting watches")
                    WatchBatteryWidget.enableWidget(this@BaseWatchPickerActivity)
                    val connectedWatchId = watchConnectionManager!!.getConnectedWatchId()
                    var selectedWatchPosition = 0
                    watches.forEach {
                        withContext(Dispatchers.Main) {
                            (watchPickerSpinner.adapter as WatchPickerAdapter).add(it)
                        }
                        if (it.id == connectedWatchId) {
                            selectedWatchPosition =
                                    (watchPickerSpinner.adapter as WatchPickerAdapter).getPosition(it)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        watchPickerSpinner.setSelection(selectedWatchPosition)
                    }
                } else {
                    Timber.i("No registered watches found")
                    WatchBatteryWidget.disableWidget(this@BaseWatchPickerActivity)
                    startSetupActivity()
                }
            } else {
                Timber.e("Failed to get list of watches")
            }
        }
    }

    /**
     * Start an instance of [WatchManagerActivity].
     */
    fun startWatchManagerActivity() {
        Timber.i("startWatchManagerActivity() called")
        Intent(this, WatchManagerActivity::class.java).also {
            startActivityForResult(it, WATCH_MANAGER_ACTIVITY_REQUEST_CODE)
        }
    }

    /**
     * Start an instance of [WatchSetupActivity].
     */
    private fun startSetupActivity() {
        Timber.i("startSetupActivity() called")
        Intent(this@BaseWatchPickerActivity, WatchSetupActivity::class.java).also {
            startActivityForResult(it, WATCH_SETUP_ACTIVITY_REQUEST_CODE)
        }
    }

    companion object {
        private const val WATCH_SETUP_ACTIVITY_REQUEST_CODE = 54321
        private const val WATCH_MANAGER_ACTIVITY_REQUEST_CODE = 65432
    }

    class WatchPickerAdapter(context: Context) : ArrayAdapter<Watch>(context, 0) {

        private val layoutInflater = LayoutInflater.from(context)

        override fun getItemId(position: Int): Long = getItem(position)?.id?.toLong(36) ?: -1

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        /**
         * Creates the [View] for an item in the spinner.
         * @param position The position of the item.
         * @param convertView The old [View] to recycle, if any.
         * @param parent The parent [ViewGroup] of the view.
         * @return The new [View] for the item.
         */
        private fun getItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            val watch = getItem(position)!!
            var view = convertView
            if (view == null) {
                view = layoutInflater.inflate(R.layout.common_spinner_item_two_line, parent, false)
            }
            view!!.apply {
                findViewById<AppCompatTextView>(R.id.title).text = watch.name
                findViewById<AppCompatTextView>(R.id.subtitle).text = context.getString(when (watch.status) {
                    WatchStatus.CONNECTED -> R.string.watch_status_connected
                    WatchStatus.DISCONNECTED -> R.string.watch_status_disconnected
                    WatchStatus.MISSING_APP -> R.string.watch_status_missing_app
                    else -> R.string.watch_status_error
                })
            }
            return view
        }
    }
}

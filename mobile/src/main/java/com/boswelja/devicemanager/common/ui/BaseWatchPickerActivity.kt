/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.ui

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
import com.boswelja.devicemanager.onboarding.ui.OnboardingActivity
import com.boswelja.devicemanager.watchmanager.SelectedWatchHandler
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * A base activity extending [BaseToolbarActivity], additionally adding a watch picker to the
 * toolbar and reporting watch selection changes.
 */
abstract class BaseWatchPickerActivity : BaseToolbarActivity(), AdapterView.OnItemSelectedListener {

    private val adapter: WatchPickerAdapter by lazy { WatchPickerAdapter(this) }
    internal val selectedWatchHandler by lazy { SelectedWatchHandler(this) }
    internal val coroutineScope = MainScope()
    internal val database by lazy { WatchDatabase.getInstance(this) }

    private lateinit var watchPickerSpinner: AppCompatSpinner

    override fun onItemSelected(
        adapterView: AdapterView<*>?,
        selectedView: View?,
        position: Int,
        id: Long
    ) {
        val connectedWatchId = id.toString(36)
        Timber.i("$connectedWatchId selected")
        selectedWatchHandler.selectWatchById(connectedWatchId)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database.watchDao().getAllObservable().observe(this) {
            if (it.isEmpty()) startSetupActivity() else setWatchList(it)
        }
        selectedWatchHandler.selectedWatch.observe(this) { it?.let { selectWatch(it.id) } }
    }

    /**
     * Checks the [watchPickerSpinner] has the correct watch selected. It should always match what's
     * selected in [WatchManager].
     */
    private fun selectWatch(watchId: String) {
        Timber.d("ensureCorrectWatchSelected() called")
        if (watchPickerSpinner.selectedItemId.toString(36) != watchId) {
            for (i in 0 until adapter.count) {
                val watch = adapter.getItem(i)
                if (watch?.id == watchId) {
                    watchPickerSpinner.setSelection(i, false)
                    return
                }
            }
        }
    }

    /** Update the list of registered watches that the user can choose from [watchPickerSpinner]. */
    private fun setWatchList(watches: List<Watch>) {
        Timber.d("updateConnectedWatches() called")
        adapter.clear()
        coroutineScope.launch(Dispatchers.Default) {
            Timber.i("Setting watches")
            val connectedWatchId = selectedWatchHandler.selectedWatch.value?.id
            var selectedWatchPosition = 0
            watches.forEach {
                withContext(Dispatchers.Main) { adapter.add(it) }
                if (it.id == connectedWatchId) {
                    selectedWatchPosition = adapter.getPosition(it)
                }
            }
            withContext(Dispatchers.Main) {
                watchPickerSpinner.setSelection(selectedWatchPosition)
            }
        }
    }

    /** Set up [watchPickerSpinner]. */
    fun setupWatchPickerSpinner(toolbar: MaterialToolbar, showUpButton: Boolean = false) {
        Timber.d("setupWatchPickerSpinner() called")
        watchPickerSpinner =
            AppCompatSpinner(this)
                .apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    onItemSelectedListener = this@BaseWatchPickerActivity
                    adapter = this@BaseWatchPickerActivity.adapter
                }
                .also {
                    toolbar.addView(it)
                    setupToolbar(toolbar, showUpButton = showUpButton)
                }
    }

    /** Start an instance of [OnboardingActivity] and finishes this activity. */
    private fun startSetupActivity() {
        Timber.d("startSetupActivity() called")
        Intent(this@BaseWatchPickerActivity, OnboardingActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    class WatchPickerAdapter(context: Context) : ArrayAdapter<Watch>(context, 0) {

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
                val layoutInflater = LayoutInflater.from(parent.context)
                view = layoutInflater.inflate(R.layout.watch_selector_item, parent, false)
            }
            view!!.findViewById<AppCompatTextView>(R.id.title).text = watch.name
            return view
        }
    }
}

/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchsetup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.watchmanager.Watch
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.WatchStatus
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchSetupFragment : Fragment() {

    private val watchConnectionManagerConnection = object : WatchManager.Connection() {
        override fun onWatchManagerBound(watchManager: WatchManager) {
            watchConnectionManager = watchManager
            refreshAvailableWatches()
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
        }
    }

    private val coroutineScope = MainScope()

    private var watchConnectionManager: WatchManager? = null

    private lateinit var refreshButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var watchSetupRecyclerView: RecyclerView
    private lateinit var helpTextView: AppCompatTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_watch_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshButton = view.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            refreshAvailableWatches()
        }
        progressBar = view.findViewById(R.id.progress_bar)
        watchSetupRecyclerView = view.findViewById(R.id.watch_setup_recyclerview)
        helpTextView = view.findViewById(R.id.help_text_view)

        setupRecyclerView()
        setLoading(true)

        WatchManager.bind(requireContext(), watchConnectionManagerConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unbindService(watchConnectionManagerConnection)
    }

    /**
     * Set up [watchSetupRecyclerView].
     */
    private fun setupRecyclerView() {
        watchSetupRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = WatchSetupAdapter(this@WatchSetupFragment)
        }
    }

    /**
     * Refresh the list of watches shown in the [WatchSetupAdapter].
     */
    private fun refreshAvailableWatches() {
        if (watchConnectionManager != null) {
            hideHelpMessage()
            setLoading(true)
            coroutineScope.launch(Dispatchers.IO) {
                val availableWatches = watchConnectionManager?.getAvailableWatches()
                withContext(Dispatchers.Main) {
                    if (availableWatches != null) {
                        if (availableWatches.isNotEmpty()) {
                            (watchSetupRecyclerView.adapter as WatchSetupAdapter)
                                    .setWatches(availableWatches)
                        } else {
                            setHelpMessage(getString(R.string.register_watch_message_no_watches))
                        }
                    } else {
                        setHelpMessage(getString(R.string.register_watch_message_error))
                    }
                    setLoading(false)
                }
            }
        }
    }

    /**
     * Sets whether the loading view should be shown.
     * @param loading true if the loading view should be shown, false otherwise
     */
    private fun setLoading(loading: Boolean) {
        refreshButton.isEnabled = !loading
        watchSetupRecyclerView.isEnabled = !loading
        progressBar.visibility = if (loading) {
            ProgressBar.VISIBLE
        } else {
            ProgressBar.INVISIBLE
        }
    }

    /**
     * Sets help text to aid the user if anything goes wrong.
     * @param text The help text to show.
     */
    private fun setHelpMessage(text: String) {
        helpTextView.visibility = AppCompatTextView.VISIBLE
        watchSetupRecyclerView.visibility = RecyclerView.INVISIBLE
        helpTextView.text = text
    }

    /**
     * Removes the help text.
     */
    private fun hideHelpMessage() {
        helpTextView.visibility = AppCompatTextView.INVISIBLE
        watchSetupRecyclerView.visibility = RecyclerView.VISIBLE
    }

    /**
     * Register a [Watch] with Wearable Extensions.
     * @param watch The [Watch] to register.
     */
    private fun registerWatch(watch: Watch) {
        coroutineScope.launch(Dispatchers.IO) {
            watchConnectionManager?.registerWatch(watch)
            withContext(Dispatchers.Main) {
                activity?.setResult(WatchSetupActivity.RESULT_WATCH_ADDED)
                activity?.finish()
            }
        }
    }

    /**
     * Asks the user whether they want to register a given [Watch].
     * @param watch The [Watch] in question.
     */
    fun confirmRegisterWatch(watch: Watch) {
        AlertDialog.Builder(requireContext()).apply {
            if (watch.status != WatchStatus.MISSING_APP) {
                setTitle(getString(R.string.register_watch_dialog_title, watch.name))
                setMessage(getString(R.string.register_watch_dialog_message, watch.name))
                setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                    registerWatch(watch)
                }
                setNegativeButton(R.string.dialog_button_no) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
            } else {
                setTitle(R.string.missing_app_dialog_title)
                setMessage(getString(R.string.missing_app_dialog_message, watch.name))
                setPositiveButton(R.string.dialog_button_ok) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
            }
        }.also {
            it.show()
        }
    }
}

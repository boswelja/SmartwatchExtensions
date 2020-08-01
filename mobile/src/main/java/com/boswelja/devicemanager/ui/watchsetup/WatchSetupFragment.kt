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
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.FragmentWatchSetupBinding
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.WatchStatus
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchSetupFragment : Fragment() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val watchManager by lazy { WatchManager.get(requireContext()) }

    private lateinit var binding: FragmentWatchSetupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWatchSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.refreshButton.setOnClickListener { refreshAvailableWatches() }
        binding.watchSetupRecyclerview.adapter = WatchSetupAdapter { confirmRegisterWatch(it) }
        setLoading(true)

        refreshAvailableWatches()
    }

    /**
     * Refresh the list of watches shown in the [WatchSetupAdapter].
     */
    private fun refreshAvailableWatches() {
        hideHelpMessage()
        setLoading(true)
        coroutineScope.launch {
            val availableWatches = watchManager.getAvailableWatches()
            withContext(Dispatchers.Main) {
                if (availableWatches != null) {
                    if (availableWatches.isNotEmpty()) {
                        (binding.watchSetupRecyclerview.adapter as WatchSetupAdapter)
                            .submitList(availableWatches)
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

    /**
     * Sets whether the loading view should be shown.
     * @param loading true if the loading view should be shown, false otherwise
     */
    private fun setLoading(loading: Boolean) {
        binding.apply {
            refreshButton.isEnabled = !loading
            watchSetupRecyclerview.isEnabled = !loading
            progressBar.visibility = if (loading) {
                ProgressBar.VISIBLE
            } else {
                ProgressBar.INVISIBLE
            }
        }
    }

    /**
     * Sets help text to aid the user if anything goes wrong.
     * @param text The help text to show.
     */
    private fun setHelpMessage(text: String) {
        binding.apply {
            helpTextView.visibility = AppCompatTextView.VISIBLE
            watchSetupRecyclerview.isEnabled = false
            helpTextView.text = text
        }
    }

    /**
     * Removes the help text.
     */
    private fun hideHelpMessage() {
        binding.apply {
            helpTextView.visibility = AppCompatTextView.INVISIBLE
            watchSetupRecyclerview.isEnabled = true
        }
    }

    /**
     * Register a [Watch] with Wearable Extensions.
     * @param watch The [Watch] to register.
     */
    private fun registerWatch(watch: Watch) {
        coroutineScope.launch {
            watchManager.registerWatch(watch)
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
    private fun confirmRegisterWatch(watch: Watch) {
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

/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.setup.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.FragmentWatchSetupBinding
import com.boswelja.devicemanager.main.MainActivity
import com.boswelja.devicemanager.watchmanager.WatchStatus
import com.boswelja.devicemanager.watchmanager.item.Watch

class WatchSetupFragment : Fragment() {

    private val adapter by lazy { WatchSetupAdapter { confirmRegisterWatch(it) } }
    private val viewModel: WatchSetupViewModel by viewModels()

    private lateinit var binding: FragmentWatchSetupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWatchSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.refreshButton.setOnClickListener { viewModel.refreshAvailableWatches() }
        binding.watchSetupRecyclerview.adapter = adapter

        viewModel.availableWatches.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                setHelpMessage(getString(R.string.register_watch_message_no_watches))
            }
            adapter.submitList(it)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) hideHelpMessage()
            setLoading(it)
        }
        viewModel.finishActivity.observe(viewLifecycleOwner) {
            if (it) {
                Intent(requireContext(), MainActivity::class.java).also { intent ->
                    startActivity(intent)
                }
                activity?.finish()
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
            progressBar.visibility =
                if (loading) {
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

    /** Removes the help text. */
    private fun hideHelpMessage() {
        binding.apply {
            helpTextView.visibility = AppCompatTextView.INVISIBLE
            watchSetupRecyclerview.isEnabled = true
        }
    }

    /**
     * Asks the user whether they want to register a given [Watch].
     * @param watch The [Watch] in question.
     */
    private fun confirmRegisterWatch(watch: Watch) {
        AlertDialog.Builder(requireContext())
            .apply {
                if (watch.status != WatchStatus.MISSING_APP) {
                    setTitle(getString(R.string.register_watch_dialog_title, watch.name))
                    setMessage(getString(R.string.register_watch_dialog_message, watch.name))
                    setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                        viewModel.registerWatch(watch)
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
            }
            .also { it.show() }
    }
}

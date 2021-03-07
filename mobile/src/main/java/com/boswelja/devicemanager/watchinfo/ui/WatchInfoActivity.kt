/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchinfo.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.connection.Capability
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityWatchInfoBinding
import timber.log.Timber

class WatchInfoActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityWatchInfoBinding

    private val forgetWatchSleet by lazy { ForgetWatchSheet() }
    private val clearPreferencesSheet by lazy { ClearWatchPreferencesSheet() }
    private val capabilitiesAdapter by lazy { CapabilitiesAdapter() }
    private val watchId by lazy { intent?.getStringExtra(EXTRA_WATCH_ID)!! }
    private val viewModel: WatchInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.setWatch(watchId)

        setupToolbar(binding.toolbarLayout.toolbar, showUpButton = true)

        binding.capabilitiesRecyclerview.adapter = capabilitiesAdapter

        setupButtons()
        setupWatchNameEditor()

        viewModel.watch.observe(this) { watch ->
            val capabilities = Capability.values().filter { watch.hasCapability(it) }
            Timber.d("Got ${capabilities.count()} capabilities")
            capabilitiesAdapter.submitList(capabilities)

            // If not recreating from saved instance state (i.e. not recreating after rotating)
            if (savedInstanceState == null) {
                Timber.d("Updating name field")
                binding.watchNameField.setText(watch.name)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        updateWatchNickname()
    }

    private fun setupButtons() {
        binding.refreshCapabilitiesButton.setOnClickListener {
            viewModel.refreshCapabilities()
            createSnackBar(getString(R.string.refresh_capabilities_requested))
        }
        binding.clearPreferencesButton.setOnClickListener {
            clearPreferencesSheet.show(
                supportFragmentManager,
                ClearWatchPreferencesSheet::class.simpleName
            )
        }
        binding.forgetWatchButton.setOnClickListener {
            forgetWatchSleet.show(supportFragmentManager, ForgetWatchSheet::class.simpleName)
        }
    }

    private fun setupWatchNameEditor() {
        binding.watchNameLayout.setOnFocusChangeListener { _, hasFocus ->
            Timber.d("watchNamelayout focus changed")
            if (!hasFocus) {
                updateWatchNickname()
            }
        }
        binding.watchNameField.doOnTextChanged { text, _, _, _ ->
            binding.watchNameLayout.apply {
                if (text.isNullOrBlank()) {
                    error = getString(R.string.watch_name_field_empty_error)
                    isErrorEnabled = true
                } else {
                    isErrorEnabled = false
                }
            }
        }
    }

    /**
     * Get current value from watch name field and update the watch name in database.
     */
    private fun updateWatchNickname() {
        if (!binding.watchNameLayout.isErrorEnabled) {
            Timber.d("Updating watch nickname")
            viewModel.updateWatchName(binding.watchNameField.text.toString())
        }
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

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
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityWatchInfoBinding
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber

class WatchInfoActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityWatchInfoBinding

    private val forgetWatchSleet by lazy { ForgetWatchSheet() }
    private val clearPreferencesSheet by lazy { ClearWatchPreferencesSheet() }

    private val watchId by lazy { intent?.getStringExtra(EXTRA_WATCH_ID)!! }
    private val viewModel: WatchInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.setWatch(watchId)

        binding.apply {
            setupToolbar(toolbarLayout.toolbar, showUpButton = true)
            clearPreferencesButton.setOnClickListener {
                clearPreferencesSheet.show(
                    supportFragmentManager,
                    ClearWatchPreferencesSheet::class.simpleName
                )
            }
            forgetWatchButton.setOnClickListener {
                forgetWatchSleet.show(supportFragmentManager, ForgetWatchSheet::class.simpleName)
            }
        }

        viewModel.watch.observe(this) {
            // If not recreating from saved instance state (i.e. not recreating after rotating)
            if (savedInstanceState == null) {
                binding.watchNameField.setText(it.name)
            }
        }

        binding.watchNameLayout.setOnFocusChangeListener { v, hasFocus ->
            // If view is text field, doesn't have focus, and doesn't have an error shown
            Timber.d("watchNamelayout focus changed")
            if (v is TextInputLayout && !hasFocus && v.isErrorEnabled) {
                Timber.d("Updating watch nickname")
                viewModel.updateWatchName(v.editText!!.text.toString())
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

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

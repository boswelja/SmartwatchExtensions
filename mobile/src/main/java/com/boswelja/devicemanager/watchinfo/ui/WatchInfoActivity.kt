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
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityWatchInfoBinding
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber

class WatchInfoActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityWatchInfoBinding

    private val watchId by lazy { intent?.getStringExtra(EXTRA_WATCH_ID)!! }
    private val viewModel: WatchInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            setupToolbar(toolbarLayout.toolbar, showUpButton = true)
            clearPreferencesButton.setOnClickListener { confirmClearPreferences() }
            forgetWatchButton.setOnClickListener { confirmForgetWatch() }
        }

        viewModel.setWatch(watchId)

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
        binding.watchNameField.doOnTextChanged { text, start, before, count ->
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

    /** Asks the user if they would like to clear preferences for their [Watch]. */
    private fun confirmClearPreferences() {
        Timber.d("confirmClearPreferences() called")
        AlertDialog.Builder(this@WatchInfoActivity)
            .apply {
                setTitle(R.string.clear_preferences_dialog_title)
                setMessage(
                    getString(
                        R.string.clear_preferences_dialog_message,
                        binding.watchNameField.text
                    )
                )
                setPositiveButton(R.string.dialog_button_yes) { _, _ -> clearPreferences() }
                setNegativeButton(R.string.dialog_button_no) { dialogInterface, _ ->
                    Timber.i("User aborted")
                    dialogInterface.dismiss()
                }
            }
            .show()
    }

    /** Clears the preferences for a given [Watch], and notifies the user of the result. */
    private fun clearPreferences() {
        Timber.d("clearPreferences() called")
        viewModel.resetWatchPreferences()
        createSnackBar("Successfully requested settings reset")
    }

    /** Asks the user if they want to forget their [Watch]. */
    private fun confirmForgetWatch() {
        Timber.d("confirmForgetWatch() called")
        AlertDialog.Builder(this@WatchInfoActivity)
            .apply {
                setTitle(R.string.forget_watch_dialog_title)
                setMessage(
                    getString(
                        R.string.forget_watch_dialog_message
                    )
                )
                setPositiveButton(R.string.dialog_button_yes) { _, _ -> viewModel.forgetWatch() }
                setNegativeButton(R.string.dialog_button_no) { dialogInterface, _ ->
                    Timber.i("User aborted")
                    dialogInterface.dismiss()
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

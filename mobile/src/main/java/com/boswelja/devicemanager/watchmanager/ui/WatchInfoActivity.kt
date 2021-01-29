/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityWatchInfoBinding
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.WatchPreferenceManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class WatchInfoActivity : BaseToolbarActivity() {

    private val coroutineScope = MainScope()
    private val watchPreferenceManager by lazy { WatchPreferenceManager.get(this) }

    private lateinit var watchManager: WatchManager
    private lateinit var binding: ActivityWatchInfoBinding

    private val watchId by lazy { intent?.getStringExtra(EXTRA_WATCH_ID)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            setupToolbar(toolbarLayout.toolbar, showUpButton = true)
            clearPreferencesButton.setOnClickListener { confirmClearPreferences() }
            forgetWatchButton.setOnClickListener { confirmForgetWatch() }
        }

        watchManager = WatchManager.getInstance(this)

        resetNicknameTextField()
    }

    /** Resets the text in the watch name field to the nickname stored in [WatchManager]. */
    private fun resetNicknameTextField() {
        val watch = watchManager.registeredWatches.value!!.firstOrNull { it.id == watchId }
        if (watch == null) {
            Timber.w("Failed to get watch")
        } else {
            binding.apply {
                watchNameField.setText(watch.name)
                watchNameField.doAfterTextChanged {
                    if (!it.isNullOrBlank()) {
                        Timber.i("Updating watch nickname")
                        watchNameLayout.isErrorEnabled = false
                        coroutineScope.launch(Dispatchers.IO) {
                            watchManager.renameWatch(watch, it.toString())
                        }
                    } else {
                        Timber.w("Invalid watch nickname")
                        watchNameLayout.isErrorEnabled = true
                        watchNameField.error = "Nickname cannot be blank"
                    }
                }
            }
        }
    }

    /** Asks the user if they would like to clear preferences for their [Watch]. */
    private fun confirmClearPreferences() {
        Timber.d("confirmClearPreferences() called")
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
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
        }
    }

    /** Clears the preferences for a given [Watch], and notifies the user of the result. */
    private fun clearPreferences() {
        Timber.d("clearPreferences() called")
        coroutineScope.launch(Dispatchers.IO) {
            val success = watchPreferenceManager.clearPreferencesForWatch(watchId)
            withContext(Dispatchers.Main) {
                if (success) {
                    Timber.i("Successfully cleared preferences")
                    createSnackBar(
                        "Successfully cleared settings for your ${binding.watchNameField.text}"
                    )
                } else {
                    Timber.w("Failed to clear preferences")
                    createSnackBar(
                        "Failed to clear settings for your ${binding.watchNameField.text}"
                    )
                }
            }
        }
    }

    /** Asks the user if they want to forget their [Watch]. */
    private fun confirmForgetWatch() {
        Timber.d("confirmForgetWatch() called")
        coroutineScope.launch(Dispatchers.IO) {
            val watch = watchManager.registeredWatches.value?.firstOrNull { it.id == watchId }
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@WatchInfoActivity)
                    .apply {
                        setTitle(R.string.forget_watch_dialog_title)
                        setMessage(
                            getString(
                                R.string.forget_watch_dialog_message, watch?.name, watch?.name
                            )
                        )
                        setPositiveButton(R.string.dialog_button_yes) { _, _ -> forgetWatch(watch) }
                        setNegativeButton(R.string.dialog_button_no) { dialogInterface, _ ->
                            Timber.i("User aborted")
                            dialogInterface.dismiss()
                        }
                    }
                    .show()
            }
        }
    }

    /**
     * Forgets a given [Watch].
     * @param watch The [Watch] to forget.
     */
    private fun forgetWatch(watch: Watch?) {
        Timber.d("forgetWatch() called")
        coroutineScope.launch(Dispatchers.IO) {
            val success = watchManager.forgetWatch(watch!!)
            withContext(Dispatchers.Main) {
                if (success) {
                    Timber.i("Successfully forgot watch")
                    finish()
                } else {
                    Timber.w("Failed to forget watch")
                    createSnackBar("Failed to forget your ${watch.name}")
                }
            }
        }
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

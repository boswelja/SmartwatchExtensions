/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.ActivityWatchInfoBinding
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class WatchInfoActivity : BaseToolbarActivity() {

    private val nicknameChangeListener = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            binding.apply {
                if (!editable.isNullOrBlank()) {
                    Timber.i("Updating watch nickname")
                    watchNameLayout.isErrorEnabled = false
                    coroutineScope.launch(Dispatchers.IO) {
                        watchManager.database.watchDao().setName(watchId!!, editable.toString())
                    }
                    resultCode = RESULT_WATCH_NAME_CHANGED
                } else {
                    Timber.w("Invalid watch nickname")
                    watchNameLayout.isErrorEnabled = true
                    watchNameField.error = "Nickname cannot be blank"
                }
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {} // Do nothing

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {} // Do nothing
    }

    private val coroutineScope = MainScope()
    private val watchDatabase: WatchDatabase by lazy { WatchDatabase.get(this) }

    private lateinit var binding: ActivityWatchInfoBinding

    private lateinit var watchManager: WatchManager
    private var watchId: String? = null

    private var resultCode = RESULT_WATCH_UNCHANGED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        watchId = intent?.getStringExtra(EXTRA_WATCH_ID)

        binding = ActivityWatchInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            setupToolbar(toolbarLayout.toolbar, showUpButton = true)
            clearPreferencesButton.setOnClickListener {
                confirmClearPreferences()
            }
            forgetWatchButton.setOnClickListener {
                confirmForgetWatch()
            }
        }

        watchManager = WatchManager.get(this)
        resetNicknameTextField()
    }

    override fun finish() {
        setResultCode()
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.watchNameField.removeTextChangedListener(nicknameChangeListener)
    }

    /**
     * Resets the text in the watch name field to the nickname stored in [WatchManager].
     */
    private fun resetNicknameTextField() {
        coroutineScope.launch(Dispatchers.IO) {
            val watchName = watchDatabase.watchDao().get(watchId!!)?.name
            withContext(Dispatchers.Main) {
                binding.apply {
                    watchNameField.setText(watchName)
                    watchNameField.addTextChangedListener(nicknameChangeListener)
                }
            }
        }
    }

    /**
     * Sets the activity result code, as well as any extras that may need to be included.
     */
    private fun setResultCode() {
        if (resultCode != RESULT_WATCH_UNCHANGED) {
            Intent().putExtra(EXTRA_WATCH_ID, watchId).also {
                setResult(resultCode, it)
            }
        } else {
            setResult(resultCode)
        }
    }

    /**
     * Asks the user if they would like to clear preferences for their [Watch].
     */
    private fun confirmClearPreferences() {
        Timber.d("confirmClearPreferences() called")
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@WatchInfoActivity).apply {
                    setTitle(R.string.clear_preferences_dialog_title)
                    setMessage(getString(R.string.clear_preferences_dialog_message, binding.watchNameField.text))
                    setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                        clearPreferences()
                    }
                    setNegativeButton(R.string.dialog_button_no) { dialogInterface, _ ->
                        Timber.i("User aborted")
                        dialogInterface.dismiss()
                    }
                }.show()
            }
        }
    }

    /**
     * Clears the preferences for a given [Watch], and notifies the user of the result.
     */
    private fun clearPreferences() {
        Timber.d("clearPreferences() called")
        coroutineScope.launch(Dispatchers.IO) {
            val success = watchManager.clearPreferencesForWatch(watchId)
            withContext(Dispatchers.Main) {
                if (success) {
                    Timber.i("Successfully cleared preferences")
                    createSnackBar("Successfully cleared settings for your ${binding.watchNameField.text}")
                } else {
                    Timber.w("Failed to clear preferences")
                    createSnackBar("Failed to clear settings for your ${binding.watchNameField.text}")
                }
            }
        }
    }

    /**
     * Asks the user if they want to forget their [Watch].
     */
    private fun confirmForgetWatch() {
        Timber.d("confirmForgetWatch() called")
        coroutineScope.launch(Dispatchers.IO) {
            val watch = watchDatabase.watchDao().get(watchId!!)
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@WatchInfoActivity).apply {
                    setTitle(R.string.forget_watch_dialog_title)
                    setMessage(getString(R.string.forget_watch_dialog_message, watch?.name, watch?.name))
                    setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                        forgetWatch(watch)
                    }
                    setNegativeButton(R.string.dialog_button_no) { dialogInterface, _ ->
                        Timber.i("User aborted")
                        dialogInterface.dismiss()
                    }
                }.show()
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
            val success = watchManager.forgetWatch(watch?.id)
            withContext(Dispatchers.Main) {
                if (success) {
                    Timber.i("Successfully forgot watch")
                    resultCode = RESULT_WATCH_REMOVED
                    finish()
                } else {
                    Timber.w("Failed to forget watch")
                    createSnackBar("Failed to forget your ${watch?.name}")
                }
            }
        }
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"

        const val RESULT_WATCH_UNCHANGED = 0
        const val RESULT_WATCH_REMOVED = 1
        const val RESULT_WATCH_NAME_CHANGED = 2
    }
}

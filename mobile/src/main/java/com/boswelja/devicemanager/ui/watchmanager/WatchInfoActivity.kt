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
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.watchmanager.Watch
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class WatchInfoActivity : BaseToolbarActivity() {

    private val watchConnectionManagerConnection = object : WatchManager.Connection() {
        override fun onWatchManagerBound(watchManager: WatchManager) {
            Timber.i("Watch manager bound")
            watchConnectionManager = watchManager
            resetNicknameTextField()
        }

        override fun onWatchManagerUnbound() {
            Timber.w("Watch manager unbound")
            watchConnectionManager = null
        }
    }

    private val nicknameChangeListener = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            if (!editable.isNullOrBlank()) {
                Timber.i("Updating watch nickname")
                watchNameLayout.isErrorEnabled = false
                coroutineScope.launch(Dispatchers.IO) {
                    watchConnectionManager?.updateWatchName(watchId!!, editable.toString())
                }
                resultCode = RESULT_WATCH_NAME_CHANGED
            } else {
                Timber.w("Invalid watch nickname")
                watchNameLayout.isErrorEnabled = true
                watchNameField.error = "Nickname cannot be blank"
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {} // Do nothing

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {} // Do nothing
    }

    private val coroutineScope = MainScope()

    private lateinit var watchNameLayout: TextInputLayout
    private lateinit var watchNameField: TextInputEditText

    private var watchConnectionManager: WatchManager? = null
    private var watchId: String? = null

    private var resultCode = RESULT_WATCH_UNCHANGED

    override fun getContentViewId(): Int = R.layout.activity_watch_info

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        watchId = intent?.getStringExtra(EXTRA_WATCH_ID)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        watchNameLayout = findViewById(R.id.watch_name_layout)
        watchNameField = findViewById(R.id.watch_name_field)
        findViewById<MaterialButton>(R.id.clear_preferences_button)?.setOnClickListener {
            confirmClearPreferences()
        }
        findViewById<MaterialButton>(R.id.forget_watch_button)?.setOnClickListener {
            confirmForgetWatch()
        }

        WatchManager.bind(this, watchConnectionManagerConnection)
    }

    override fun finish() {
        setResultCode()
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(watchConnectionManagerConnection)
        watchNameField.removeTextChangedListener(nicknameChangeListener)
    }

    /**
     * Resets the text in [watchNameField] to the nickname stored in [WatchManager].
     */
    private fun resetNicknameTextField() {
        coroutineScope.launch(Dispatchers.IO) {
            val watchName = watchConnectionManager?.getWatchById(watchId)?.name
            withContext(Dispatchers.Main) {
                watchNameField.setText(watchName)
                watchNameField.addTextChangedListener(nicknameChangeListener)
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
            val watch = watchConnectionManager?.getWatchById(watchId)
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@WatchInfoActivity).apply {
                    setTitle(R.string.clear_preferences_dialog_title)
                    setMessage(getString(R.string.clear_preferences_dialog_message, watch?.name))
                    setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                        clearPreferences(watch)
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
     * @param watch the [Watch] to clear preferences for.
     */
    private fun clearPreferences(watch: Watch?) {
        Timber.d("clearPreferences() called")
        coroutineScope.launch(Dispatchers.IO) {
            val success = watchConnectionManager?.clearPreferencesForWatch(watch?.id) == true
            withContext(Dispatchers.Main) {
                if (success) {
                    Timber.i("Successfully cleared preferences")
                    createSnackBar("Successfully cleared settings for your ${watch?.name}")
                } else {
                    Timber.w("Failed to clear preferences")
                    createSnackBar("Failed to clear settings for your ${watch?.name}")
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
            val watch = watchConnectionManager?.getWatchById(watchId)
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
            val success =
                    watchConnectionManager?.forgetWatch(watch?.id) == true
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

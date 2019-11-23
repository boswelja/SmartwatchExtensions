package com.boswelja.devicemanager.ui.watchmanager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class WatchInfoActivity : BaseToolbarActivity() {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service

            watchNameField.setText(service.getWatchById(watchId)?.name)
            watchNameField.addTextChangedListener(nicknameChangeListener)
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
            watchNameField.removeTextChangedListener(nicknameChangeListener)
        }
    }

    private val nicknameChangeListener = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            if (!editable.isNullOrBlank()) {
                watchNameLayout.isErrorEnabled = false
                watchConnectionManager?.updateWatchNickname(watchId!!, editable.toString())
            } else {
                watchNameLayout.isErrorEnabled = true
                watchNameField.error = "Nickname cannot be blank"
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {} // Do nothing

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {} // Do nothing
    }

    private var watchConnectionManager: WatchConnectionService? = null
    private var watchId: String? = null

    private lateinit var watchNameLayout: TextInputLayout
    private lateinit var watchNameField: TextInputEditText

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
            MaterialAlertDialogBuilder(this)
                    .setBackground(getDrawable(R.drawable.dialog_background))
                    .setTitle(R.string.clear_preferences_dialog_title)
                    .setMessage(getString(R.string.clear_preferences_dialog_message, watchConnectionManager?.getWatchById(watchId)?.name))
                    .setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                        watchConnectionManager?.clearPreferencesForWatch(watchId)
                    }
                    .setNegativeButton(R.string.dialog_button_no) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .show()
        }
    }

    override fun onResume() {
        super.onResume()
        WatchConnectionService.bind(this, watchConnectionManagerConnection)
    }

    override fun onPause() {
        super.onPause()
        unbindService(watchConnectionManagerConnection)
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}
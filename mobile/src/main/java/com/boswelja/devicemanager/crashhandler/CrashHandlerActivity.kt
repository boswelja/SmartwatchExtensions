/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.crashhandler

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.MainApplication
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.ActivityCrashHandlerBinding
import com.boswelja.devicemanager.ui.main.MainActivity
import timber.log.Timber

class CrashHandlerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrashHandlerBinding
    private var stacktrace: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_crash_handler)

        stacktrace = intent.getStringExtra(EXTRA_STACKTRACE)

        setupActions()
        setupStacktraceView()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainApplication.killCurrentProcess()
    }

    /**
     * Set up click listeners for actions.
     */
    private fun setupActions() {
        binding.apply {
            restartButton.setOnClickListener {
                restartApp()
            }

            reportButton.setOnClickListener {
                composeCrashReportEmail()
            }

            closeButton.setOnClickListener {
                Timber.i("Closing")
                finish()
            }
        }
    }

    /**
     * Set up Stacktrace view.
     */
    private fun setupStacktraceView() {
        binding.apply {
            stacktraceTextView.text = stacktrace
            stacktraceActionView.setOnClickListener {
                if (stacktraceScrollView.visibility != View.VISIBLE) {
                    stacktraceScrollView.visibility = View.VISIBLE
                    stacktraceLabel.text = getString(R.string.crash_handler_hide_stacktrace)
                    expandCollapseIndicator.setImageResource(R.drawable.ic_collapse)
                } else {
                    stacktraceScrollView.visibility = View.GONE
                    stacktraceLabel.text = getString(R.string.crash_handler_show_stacktrace)
                    expandCollapseIndicator.setImageResource(R.drawable.ic_expand)
                }
            }
        }
    }

    /**
     * Compose an email containing information about the system environment,
     * as well as the Stacktrace, to me (the developer) ready to send.
     */
    private fun composeCrashReportEmail() {
        val emailBody = "App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                "System API Level: ${Build.VERSION.SDK_INT}\n" +
                "Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.HARDWARE})\n\n" +
                "Stacktrace:\n${stacktrace}\n\n" +
                "Steps to reproduce (if applicable):\n\n"
        Intent(Intent.ACTION_SENDTO).apply {
            type = "text/plain"
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf("boswelja78@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Wearable Extensions crash")
            putExtra(Intent.EXTRA_TEXT, emailBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            if (it.resolveActivity(packageManager) != null) {
                Timber.i("Launching email program to report error")
                startActivity(it)
                finish()
            } else {
                Timber.w("No email client found")
            }
        }
    }

    /**
     * Closes this [CrashHandlerActivity] and starts a new [MainActivity] instance.
     */
    private fun restartApp() {
        Timber.i("Restarting app")
        Intent(this@CrashHandlerActivity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            startActivity(it)
        }
        finish()
    }

    companion object {
        const val EXTRA_STACKTRACE = "extra_stacktrace"
    }
}

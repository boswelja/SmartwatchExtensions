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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.ActivityCrashHandlerBinding
import com.boswelja.devicemanager.ui.main.MainActivity
import timber.log.Timber
import kotlin.system.exitProcess

class CrashHandlerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrashHandlerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_crash_handler)

        val stacktrace = intent.getStringExtra(EXTRA_STACKTRACE)

        binding.apply {
            restartButton.setOnClickListener {
                Timber.i("Restarting app")
                Intent(this@CrashHandlerActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.also {
                    startActivity(it)
                    finish()
                }
            }

            reportButton.setOnClickListener {
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

            closeButton.setOnClickListener {
                Timber.i("Closing")
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)

    }

    companion object {
        const val EXTRA_STACKTRACE = "extra_stacktrace"
    }
}

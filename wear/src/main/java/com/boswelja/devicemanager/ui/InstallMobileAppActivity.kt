/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.wearable.phone.PhoneDeviceType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.widget.CircularProgressLayout
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.boswelja.devicemanager.R
import com.google.android.wearable.intent.RemoteIntent
import com.google.android.wearable.playstore.PlayStoreAvailability

class InstallMobileAppActivity : AppCompatActivity() {

    private lateinit var circularProgressLayout: CircularProgressLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_install)

        circularProgressLayout = findViewById<CircularProgressLayout>(R.id.cancel).apply {
            onTimerFinishedListener = CircularProgressLayout.OnTimerFinishedListener {
                terminateApp(true)
            }
            setOnClickListener {
                terminateApp(false)
            }
            totalTime = 7500
            startTimer()
        }
    }

    private fun terminateApp(showPlayStoreOnPhone: Boolean) {
        circularProgressLayout.stopTimer()
        if (showPlayStoreOnPhone) {
            val isAndroidPhone = PhoneDeviceType.getPhoneDeviceType(this) == PhoneDeviceType.DEVICE_TYPE_ANDROID
            if (isAndroidPhone) {
                val playStoreIntent = Intent(Intent.ACTION_VIEW)
                if (PlayStoreAvailability.getPlayStoreAvailabilityOnPhone(this@InstallMobileAppActivity) == PlayStoreAvailability.PLAY_STORE_ON_PHONE_AVAILABLE) {
                    playStoreIntent.data = Uri.parse(String.format(getString(R.string.play_store_app_link), BuildConfig.APPLICATION_ID))
                } else {
                    playStoreIntent.data = Uri.parse(String.format(getString(R.string.play_store_web_link), BuildConfig.APPLICATION_ID))
                }
                playStoreIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                RemoteIntent.startRemoteActivity(this@InstallMobileAppActivity,
                        playStoreIntent,
                        null)
                ConfirmationActivityHandler.openOnPhoneAnimation(this)
            } else {
                Toast.makeText(this, getString(R.string.ios_incompatible_message), Toast.LENGTH_LONG).show()
            }
        }
        finishAndRemoveTask()
    }
}

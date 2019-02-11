/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
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
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.activity.ConfirmationActivity
import androidx.wear.widget.CircularProgressLayout
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.google.android.wearable.intent.RemoteIntent
import com.google.android.wearable.playstore.PlayStoreAvailability

class ConfirmInstallActivity : AppCompatActivity(), CircularProgressLayout.OnTimerFinishedListener, View.OnClickListener {

    private var header: TextView? = null
    private var description: TextView? = null
    private var circularProgressLayout: CircularProgressLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_install)

        header = findViewById(R.id.heading)
        description = findViewById(R.id.description)
        circularProgressLayout = findViewById<CircularProgressLayout>(R.id.cancel).apply {
            onTimerFinishedListener = this@ConfirmInstallActivity
            setOnClickListener(this@ConfirmInstallActivity)
            totalTime = 5000
            startTimer()
        }
    }

    override fun onTimerFinished(layout: CircularProgressLayout?) {
        terminateApp(true)
    }

    override fun onClick(v: View?) {
        terminateApp(false)
    }

    private fun terminateApp(showPlayStoreOnPhone: Boolean) {
        circularProgressLayout?.stopTimer()
        if (showPlayStoreOnPhone) {
            val isAndroidPhone = PhoneDeviceType.getPhoneDeviceType(this) == PhoneDeviceType.DEVICE_TYPE_ANDROID
            if (isAndroidPhone) {
                val intent = Intent(this, ConfirmationActivity::class.java)
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.OPEN_ON_PHONE_ANIMATION)
                val playStoreIntent = Intent(Intent.ACTION_VIEW)
                if (PlayStoreAvailability.getPlayStoreAvailabilityOnPhone(this@ConfirmInstallActivity) == PlayStoreAvailability.PLAY_STORE_ON_PHONE_AVAILABLE) {
                    playStoreIntent.data = Uri.parse(String.format(getString(R.string.play_store_app_link), BuildConfig.APPLICATION_ID))
                } else {
                    playStoreIntent.data = Uri.parse(String.format(getString(R.string.play_store_web_link), BuildConfig.APPLICATION_ID))
                }
                playStoreIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                RemoteIntent.startRemoteActivity(this@ConfirmInstallActivity,
                        playStoreIntent,
                        null)
                startActivity(intent)
            } else {
                Toast.makeText(this, "iOS isn't supported by this app", Toast.LENGTH_LONG).show()
            }
        }
        finishAndRemoveTask()
    }
}
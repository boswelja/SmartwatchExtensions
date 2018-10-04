/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.NotificationChannel
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.wearable.activity.ConfirmationActivity
import android.util.Log
import androidx.core.app.NotificationCompat
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

class ActionService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private lateinit var action: String

    override fun onCreate() {
        super.onCreate()
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel.DEFAULT_CHANNEL_ID
        } else {
            "default"
        }
        val notification: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
        notification.setContentTitle("Communicating with your phone")
        startForeground(312, notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        action = intent!!.getStringExtra(References.INTENT_ACTION_EXTRA)
        val capabilityCallback = object : Utils.CapabilityCallbacks {
            override fun noCapableDevices() {
                when (action) {
                    References.LOCK_PHONE_PATH -> onFailed("Failed to lock your phone")
                    References.REQUEST_BATTERY_UPDATE_PATH -> onFailed("Failed to update phone battery stats")
                }
            }

            override fun capableDeviceFound(node: Node?) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@ActionService)
                when (action) {
                    References.LOCK_PHONE_PATH -> {
                        if (prefs.getBoolean(PreferenceKey.LOCK_PHONE_ENABLED, false)) {
                            sendMessage(node)
                        } else {
                            onFailed("Phone locking has been disabled")
                        }
                    }
                    References.REQUEST_BATTERY_UPDATE_PATH -> {
                        sendMessage(node)
                    }
                }
            }
        }
        Utils.isCompanionAppInstalled(this, capabilityCallback)
        return START_NOT_STICKY
    }

    private fun onFailed(message: String) {
        Log.d("ActionService", message)
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION)
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, message)
        startActivity(intent)
        stopForeground(true)
        stopSelf()
    }

    private fun onSuccess(message: String) {
        Log.d("ActionService", message)
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, message)
        startActivity(intent)
        stopForeground(true)
        stopSelf()
    }

    private fun sendMessage(node: Node?) {
        Wearable.getMessageClient(this)
                .sendMessage(
                    node!!.id,
                    action,
                    null
                )
                .addOnSuccessListener {
                    onSuccess("Request succeeded")
                }
                .addOnFailureListener {
                    onFailed("Request failed")
                }
    }
}
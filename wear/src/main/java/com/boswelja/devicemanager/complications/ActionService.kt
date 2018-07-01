/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.complications

import android.app.NotificationChannel
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.support.wearable.activity.ConfirmationActivity
import android.util.Log
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.Utils
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
        notification.setContentTitle("Locking your phone...")
        startForeground(312, notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        action = intent!!.getStringExtra(References.INTENT_ACTION_EXTRA)
        val capabilityCallback = object : Utils.CapabilityCallbacks {
            override fun noCapableDevices() {
                onFailed()
            }

            override fun capableDeviceFound(node: Node?) {
                lockDevice(node)
            }
        }
        Utils.isCompanionAppInstalled(this, capabilityCallback)
        return START_NOT_STICKY
    }

    private fun onFailed() {
        Log.d("ActionService", "Failed to lock phone")
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION)
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Failed to lock your phone")
        startActivity(intent)
        stopForeground(true)
        stopSelf()
    }

    private fun onSuccess() {
        Log.d("ActionService", "Phone locked")
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Locked your phone")
        startActivity(intent)
        stopForeground(true)
        stopSelf()
    }

    private fun lockDevice(node: Node?) {
        Wearable.getMessageClient(this)
                .sendMessage(
                    node!!.id,
                    action,
                    null
                )
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener {
                    onFailed()
                }
    }
}
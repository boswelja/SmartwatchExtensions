/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.wearable.complications.ProviderUpdateRequester
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.AtomicCounter
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.batterysync.BatterySyncReferences
import com.boswelja.devicemanager.complication.PhoneBatteryComplicationProvider
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

class ActionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    private lateinit var action: String

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        val notification: NotificationCompat.Builder =
                NotificationCompat.Builder(this, ACTION_SERVICE_NOTI_CHANNEL_ID).apply {
                    setContentTitle(getString(R.string.noti_action_service_title))
                    setContentText(getString(R.string.noti_action_service_content))
                    setSmallIcon(R.drawable.ic_sync)
                }
        startForeground(AtomicCounter.getInt(), notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        action = intent!!.getStringExtra(EXTRA_ACTION)

        Utils.getCompanionNode(this)
                .addOnCompleteListener {
                    if (it.isSuccessful && !it.result?.nodes.isNullOrEmpty()) {
                        val node = it.result?.nodes?.last()
                        val prefs = PreferenceManager.getDefaultSharedPreferences(this@ActionService)
                        when (action) {
                            References.LOCK_PHONE_PATH -> {
                                if (prefs.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)) {
                                    sendMessage(node)
                                } else {
                                    ConfirmationActivityHandler.failAnimation(this@ActionService, getString(R.string.phone_lock_disabled_message))
                                }
                                val providerUpdateRequester = ProviderUpdateRequester(this@ActionService, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name))
                                providerUpdateRequester.requestUpdateAll()
                            }
                            BatterySyncReferences.REQUEST_BATTERY_UPDATE_PATH -> {
                                sendMessage(node)
                            }
                        }
                    } else {
                        when (action) {
                            References.LOCK_PHONE_PATH -> ConfirmationActivityHandler.failAnimation(this@ActionService, getString(R.string.phone_lock_failed_message))
                            BatterySyncReferences.REQUEST_BATTERY_UPDATE_PATH -> ConfirmationActivityHandler.failAnimation(this@ActionService, getString(R.string.phone_battery_update_failed))
                        }
                    }
                }

        return START_NOT_STICKY
    }

    private fun sendMessage(node: Node?) {
        Wearable.getMessageClient(this)
                .sendMessage(node!!.id, action, null)
                .addOnSuccessListener {
                    ConfirmationActivityHandler.successAnimation(this, getString(R.string.request_success_message))
                }
                .addOnFailureListener {
                    ConfirmationActivityHandler.failAnimation(this, getString(R.string.request_failed_message))
                }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    ACTION_SERVICE_NOTI_CHANNEL_ID,
                    getString(R.string.noti_channel_action_service_title),
                    NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
        }
    }

    companion object {
        const val EXTRA_ACTION = "action"

        private const val ACTION_SERVICE_NOTI_CHANNEL_ID = "action_service"
    }
}

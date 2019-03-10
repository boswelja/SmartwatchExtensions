/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

@RequiresApi(Build.VERSION_CODES.M)
class DnDLocalChangeListener : Service() {

    private lateinit var prefs: SharedPreferences
    private lateinit var notificationManager: NotificationManager
    private var dndChangeReceiver: DnDChangeReceiver? = null
    private val prefChangeListener: PreferenceChangeListener = PreferenceChangeListener()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(References.DND_SYNC_NOTI_CHANNEL_ID) == null) {
            val notiChannel = NotificationChannel(
                    References.DND_SYNC_NOTI_CHANNEL_ID,
                    getString(R.string.dnd_sync_noti_channel_name),
                    NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(notiChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notiBuilder = NotificationCompat.Builder(this, References.DND_SYNC_NOTI_CHANNEL_ID)
                .setContentTitle(getString(R.string.dnd_sync_active_noti_title))
                .setContentText(getString(R.string.dnd_sync_active_noti_desc))
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        startForeground(155216, notiBuilder)

        val intentFilter = IntentFilter()
        intentFilter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        if (dndChangeReceiver == null) dndChangeReceiver = DnDChangeReceiver()
        registerReceiver(dndChangeReceiver, intentFilter)

        updateDnD()

        return START_STICKY
    }

    private fun updateDnD() {
        val currentInterruptFilter = notificationManager.currentInterruptionFilter
        val dndEnabled: Boolean =
                (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) ||
                        (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) ||
                        (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
        val dataClient = Wearable.getDataClient(this)
        val putDataMapReq = PutDataMapRequest.create(References.DND_STATUS_KEY)
        putDataMapReq.dataMap.putBoolean(References.NEW_DND_STATE_KEY, dndEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }

    override fun onDestroy() {
        if (dndChangeReceiver != null) {
            try {
                unregisterReceiver(dndChangeReceiver)
            } catch (ignored: IllegalArgumentException) {}
        }
        super.onDestroy()
    }

    private inner class PreferenceChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
            when (key) {
                PreferenceKey.DND_SYNC_SEND_KEY -> {
                    if (!prefs?.getBoolean(key, false)!!) {
                        stopForeground(true)
                    }
                }
            }
        }
    }

    private inner class DnDChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
                updateDnD()
            }
        }
    }
}
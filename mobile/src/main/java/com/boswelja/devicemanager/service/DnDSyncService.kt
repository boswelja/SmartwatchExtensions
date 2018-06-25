/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class DnDSyncService : Service() {

    private val binder = ServiceBinder()
    private lateinit var notificationManager: NotificationManager
    private val dndChangeReceiver = DnDChangeReceiver()
    private lateinit var prefs: SharedPreferences

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        notificationManager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(References.DND_SYNC_NOTIFICATION_CHANNEL_ID)
            if (notificationManager.getNotificationChannel(References.DND_SYNC_NOTIFICATION_CHANNEL_ID) == null) {
                val notiChannel = NotificationChannel(References.DND_SYNC_NOTIFICATION_CHANNEL_ID, "DnD Sync", NotificationManager.IMPORTANCE_NONE)
                notiChannel.enableLights(false)
                notiChannel.enableVibration(false)
                notiChannel.setShowBadge(false)
                notificationManager.createNotificationChannel(notiChannel)
            }
            if (Build.VERSION.CODENAME == "P" && !prefs.getBoolean(References.DND_SYNC_WHEN_PRIORITY_ONLY, false)) {
                prefs.edit().putBoolean(References.DND_SYNC_WHEN_PRIORITY_ONLY, true).apply()
            }
        }
        val notiBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, References.DND_SYNC_NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setContentTitle("DnD Sync Active")
                .setContentText("Syncing Do Not Disturb status between your phone and watch")
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
        startForeground(155216, notiBuilder.build())


        val intentFilter = IntentFilter()
        intentFilter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        registerReceiver(dndChangeReceiver, intentFilter)
    }

    fun destroy() {
        unregisterReceiver(dndChangeReceiver)
        stopForeground(true)
        stopSelf()
    }

    private fun setWatchDnD(isActive: Boolean) {
        val dataClient = Wearable.getDataClient(this)
        val putDataMapReq = PutDataMapRequest.create("/dndStatus")
        putDataMapReq.dataMap.putBoolean("com.boswelja.devicemanager.dndenabled", isActive)
        val putDataReq = putDataMapReq.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
    }

    private inner class DnDChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
                when (notificationManager.currentInterruptionFilter) {
                    NotificationManager.INTERRUPTION_FILTER_ALARMS -> {
                        setWatchDnD(prefs.getBoolean(References.DND_SYNC_WHEN_ALARMS_ONLY, true))
                    }
                    NotificationManager.INTERRUPTION_FILTER_ALL -> {
                        setWatchDnD(false)
                    }
                    NotificationManager.INTERRUPTION_FILTER_NONE -> {
                        setWatchDnD(prefs.getBoolean(References.DND_SYNC_WHEN_TOTAL_SILENCE, true))
                    }
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY -> {
                        setWatchDnD(prefs.getBoolean(References.DND_SYNC_WHEN_PRIORITY_ONLY, false))
                    }
                    NotificationManager.INTERRUPTION_FILTER_UNKNOWN -> {
                        setWatchDnD(false)
                    }
                }
            }
        }
    }

    inner class ServiceBinder : Binder() {
        fun getService(): DnDSyncService {
            return this@DnDSyncService
        }
    }
}
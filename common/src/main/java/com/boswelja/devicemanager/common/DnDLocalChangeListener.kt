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
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager

@RequiresApi(Build.VERSION_CODES.M)
class DnDLocalChangeListener : Service() {

    private lateinit var prefs: SharedPreferences
    private lateinit var notificationManager: NotificationManager
    private var dndChangeReceiver: DnDChangeReceiver? = null
    private val prefChangeListener: PreferenceChangeListener = PreferenceChangeListener()
    private var sendPrefKey = ""

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val isPhone = resources.getBoolean(R.bool.deviceIsPhone)
        sendPrefKey = if (isPhone) {
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY
        } else {
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
        }

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
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val notiTapIntent = PendingIntent.getActivity(this, AtomicCounter.getInt(), launchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notiBuilder = NotificationCompat.Builder(this, References.DND_SYNC_NOTI_CHANNEL_ID)
                .setContentTitle(getString(R.string.dnd_sync_active_noti_title))
                .setContentText(getString(R.string.dnd_sync_active_noti_desc))
                .setContentIntent(notiTapIntent)
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build()
        startForeground(AtomicCounter.getInt(), notiBuilder)

        val intentFilter = IntentFilter()
        intentFilter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        if (dndChangeReceiver == null) dndChangeReceiver = DnDChangeReceiver()
        registerReceiver(dndChangeReceiver, intentFilter)

        CommonUtils.updateInterruptionFilter(this)

        return START_STICKY
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
            if (key == sendPrefKey &&
                    !prefs?.getBoolean(key, false)!!) {
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private inner class DnDChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
                CommonUtils.updateInterruptionFilter(this@DnDLocalChangeListener)
            }
        }
    }
}
/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.interruptfiltersync

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
import com.boswelja.devicemanager.common.AtomicCounter
import com.boswelja.devicemanager.common.R
import com.boswelja.devicemanager.common.interruptfiltersync.InterruptFilterSyncUtils.updateInterruptionFilter

@RequiresApi(Build.VERSION_CODES.M)
abstract class BaseInterruptFilterLocalChangeListener : Service() {

    private lateinit var prefs: SharedPreferences
    private lateinit var notificationManager: NotificationManager
    private var dndChangeReceiver: DnDChangeReceiver? = null
    private val prefChangeListener: PreferenceChangeListener = PreferenceChangeListener()

    abstract val interruptFilterSendEnabledKey: String

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(INTERRUPT_FINTER_SYNC_NOTI_CHANNEL_ID) == null) {
            val notiChannel = NotificationChannel(
                    INTERRUPT_FINTER_SYNC_NOTI_CHANNEL_ID,
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
        val notiBuilder = NotificationCompat.Builder(this, INTERRUPT_FINTER_SYNC_NOTI_CHANNEL_ID)
                .setContentTitle(getString(R.string.dnd_sync_active_noti_title))
                .setContentText(getString(R.string.dnd_sync_active_noti_desc))
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            val notiTapIntent = PendingIntent.getActivity(this, AtomicCounter.getInt(), launchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            notiBuilder.setContentIntent(notiTapIntent)
        }

        startForeground(AtomicCounter.getInt(), notiBuilder.build())

        val intentFilter = IntentFilter()
        intentFilter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        if (dndChangeReceiver == null) dndChangeReceiver = DnDChangeReceiver()
        registerReceiver(dndChangeReceiver, intentFilter)

        updateInterruptionFilter(this)

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
            if (key == interruptFilterSendEnabledKey &&
                    !prefs?.getBoolean(key, false)!!) {
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private inner class DnDChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
                updateInterruptionFilter(this@BaseInterruptFilterLocalChangeListener)
            }
        }
    }

    companion object {
        private const val INTERRUPT_FINTER_SYNC_NOTI_CHANNEL_ID = "dnd_sync"
    }
}

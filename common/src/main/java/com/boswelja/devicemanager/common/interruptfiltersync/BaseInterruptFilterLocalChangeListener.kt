/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.interruptfiltersync

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import com.boswelja.devicemanager.common.interruptfiltersync.References.INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID
import com.boswelja.devicemanager.common.interruptfiltersync.Utils.createNotiChannel
import com.boswelja.devicemanager.common.interruptfiltersync.Utils.updateInterruptionFilter

@RequiresApi(Build.VERSION_CODES.M)
abstract class BaseInterruptFilterLocalChangeListener : Service() {

    private lateinit var preferences: SharedPreferences
    private var interruptFilterChangeReceiver = object : InterruptFilterChangeReceiver() {
        override fun onInterruptFilterChanged(context: Context, interruptFilterEnabled: Boolean) {
            updateInterruptionFilter(this@BaseInterruptFilterLocalChangeListener, interruptFilterEnabled)
        }
    }
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == interruptFilterSendEnabledKey &&
                !sharedPreferences.getBoolean(key, false)) {
            stopForeground(true)
            stopSelf()
        }
    }

    abstract val interruptFilterSendEnabledKey: String

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(interruptFilterSendEnabledKey, false)) {
            preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotiChannel(this)
            }
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(AtomicCounter.getInt(), createNotification())

        val intentFilter = IntentFilter().apply {
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        }
        registerReceiver(interruptFilterChangeReceiver, intentFilter)

        updateInterruptionFilter(this)

        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(interruptFilterChangeReceiver)
        } catch (ignored: IllegalArgumentException) {}
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val notiTapIntent = PendingIntent.getActivity(this,
                AtomicCounter.getInt(),
                packageManager.getLaunchIntentForPackage(packageName),
                PendingIntent.FLAG_CANCEL_CURRENT)

        return NotificationCompat.Builder(this, INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID)
                .setContentTitle(getString(R.string.interrupt_filter_sync_active_noti_title))
                .setContentText(getString(R.string.interrupt_filter_sync_to_phone_noti_desc))
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setContentIntent(notiTapIntent)
                .build()
    }
}

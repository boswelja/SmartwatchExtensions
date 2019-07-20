/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.AtomicCounter
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.R
import com.boswelja.devicemanager.common.interruptfiltersync.InterruptFilterChangeReceiver
import com.boswelja.devicemanager.common.interruptfiltersync.Utils
import com.boswelja.devicemanager.ui.main.MainActivity

class InterruptFilterLocalChangeListener : Service() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    private var interruptFilterSyncToPhone: Boolean = false
    private var interruptFilterSyncWithTheater: Boolean = false
    private var interruptFilterChangeReceiver = object : InterruptFilterChangeReceiver() {
        override fun onInterruptFilterChanged(context: Context, interruptFilterEnabled: Boolean) {
            Utils.updateInterruptionFilter(this@InterruptFilterLocalChangeListener, interruptFilterEnabled)
        }
    }

    private val notificationId: Int = AtomicCounter.getInt()
    private val theaterModeObserver = TheaterModeObserver(Handler())
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            INTERRUPT_FILTER_SYNC_TO_PHONE_KEY -> {
                setInterruptFilterToPhone(sharedPreferences.getBoolean(key, false))
            }
            INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                setInterruptFilterOnWithTheater(sharedPreferences.getBoolean(key, false))
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        setInterruptFilterToPhone(sharedPreferences.getBoolean(INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false))
        setInterruptFilterOnWithTheater(sharedPreferences.getBoolean(INTERRUPT_FILTER_ON_WITH_THEATER_KEY, false))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID) == null) {
            val notiChannel = NotificationChannel(
                    INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID,
                    getString(R.string.interrupt_filter_sync_noti_channel_name),
                    NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(notiChannel)
        }
        startForeground(notificationId, createNotification())
        Utils.updateInterruptionFilter(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        NotificationCompat.Builder(this, INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID).apply {
            setContentTitle(getString(R.string.interrupt_filter_sync_active_noti_title))
            when {
                interruptFilterSyncToPhone and interruptFilterSyncWithTheater -> setContentText(getString(R.string.interrupt_filter_sync_all_noti_desc))
                interruptFilterSyncToPhone and !interruptFilterSyncWithTheater -> setContentText(getString(R.string.interrupt_filter_sync_to_phone_noti_desc))
                interruptFilterSyncWithTheater and !interruptFilterSyncToPhone -> setContentText(getString(R.string.interrupt_filter_sync_with_theater_noti_desc))
                else -> setContentText(getString(R.string.interrupt_filter_sync_none_noti_desc))
            }
            setSmallIcon(R.drawable.ic_sync)
            setOngoing(true)
            setShowWhen(false)
            setUsesChronometer(false)
            priority = NotificationCompat.PRIORITY_LOW

            val launchIntent = Intent(this@InterruptFilterLocalChangeListener, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            PendingIntent.getActivity(this@InterruptFilterLocalChangeListener,
                    AtomicCounter.getInt(),
                    launchIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT).also {
                setContentIntent(it)
            }
        }.also {
            return it.build()
        }
    }

    private fun setInterruptFilterOnWithTheater(enabled: Boolean) {
        if (interruptFilterSyncWithTheater != enabled) {
            interruptFilterSyncWithTheater = enabled
            notificationManager.notify(notificationId, createNotification())
            if (enabled) {
                applicationContext.contentResolver.registerContentObserver(
                        Settings.Global.CONTENT_URI, true,
                        theaterModeObserver)
            } else {
                applicationContext.contentResolver.unregisterContentObserver(theaterModeObserver)
                stopIfNeeded()
            }
        }
    }

    private fun setInterruptFilterToPhone(enabled: Boolean) {
        if (interruptFilterSyncToPhone != enabled) {
            interruptFilterSyncToPhone = enabled
            notificationManager.notify(notificationId, createNotification())
            if (enabled) {
                val intentFilter = IntentFilter().apply {
                    addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
                }
                registerReceiver(interruptFilterChangeReceiver, intentFilter)
            } else {
                try {
                    unregisterReceiver(interruptFilterChangeReceiver)
                } catch (ignored: IllegalArgumentException) {}
                stopIfNeeded()
            }
        }
    }

    private fun stopIfNeeded() {
        if (!interruptFilterSyncToPhone and !interruptFilterSyncWithTheater) {
            stopForeground(true)
            stopSelf()
        }
    }

    private inner class TheaterModeObserver(handler: Handler) : ContentObserver(handler) {

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val context = this@InterruptFilterLocalChangeListener
            val isTheaterModeOn = isTheaterModeOn(context)
            Utils.updateInterruptionFilter(context, isTheaterModeOn)
        }

        private fun isTheaterModeOn(context: Context): Boolean =
                Settings.Global.getInt(context.contentResolver, "theater_mode_on", 0) == 1
    }

    companion object {
        private const val INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID = "dnd_sync"
    } }

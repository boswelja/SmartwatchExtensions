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
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.AtomicCounter
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.interruptfiltersync.InterruptFilterSyncUtils.updateInterruptionFilter

class InterruptFilterSyncWithTheaterListener : Service() {

    private val dndSyncWithTheaterModeNotiCategoryKey = "dnd_sync_with_theater_mode"

    private val theaterModeObserver = TheaterModeObserver(Handler())
    private val preferenceChangeListener = PreferenceChangeListener()

    private lateinit var sharedPrefs: SharedPreferences

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        applicationContext.contentResolver.registerContentObserver(
                Settings.Global.CONTENT_URI, true,
                theaterModeObserver)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                    dndSyncWithTheaterModeNotiCategoryKey,
                    getString(R.string.dnd_sync_with_theater_mode_noti_title),
                    NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            notiManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noti = NotificationCompat.Builder(this, dndSyncWithTheaterModeNotiCategoryKey)
                .setContentTitle(getString(R.string.dnd_sync_with_theater_mode_noti_title))
                .setContentText(getString(R.string.dnd_sync_with_theater_mode_noti_content))
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        startForeground(AtomicCounter.getInt(), noti)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationContext.contentResolver.unregisterContentObserver(theaterModeObserver)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private inner class PreferenceChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY &&
                    !sharedPreferences?.getBoolean(key, false)!!) {
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private inner class TheaterModeObserver(handler: Handler) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val context = this@InterruptFilterSyncWithTheaterListener
            val isTheaterModeOn = Utils.isTheaterModeOn(context)
            updateInterruptionFilter(context, isTheaterModeOn)
        }
    }
}

/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.app.Notification
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
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.dndsync.References.DND_SYNC_LOCAL_NOTI_ID
import com.boswelja.devicemanager.common.dndsync.References.DND_SYNC_NOTI_CHANNEL_ID
import com.boswelja.devicemanager.common.dndsync.References.START_ACTIVITY_FROM_NOTI_ID
import com.boswelja.devicemanager.common.dndsync.Utils
import com.boswelja.devicemanager.main.ui.MainActivity

class DnDLocalChangeListener : Service() {

  private lateinit var sharedPreferences: SharedPreferences
  private lateinit var notificationManager: NotificationManager

  private var dndSyncToPhone: Boolean = false
  private var dndSyncWithTheater: Boolean = false

  private var dndChangeReceiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          if (context != null &&
              intent!!.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
            val dndEnabled = Compat.isDndEnabled(context)
            Utils.updateInterruptionFilter(this@DnDLocalChangeListener, dndEnabled)
          }
        }
      }

  private val theaterModeObserver = TheaterModeObserver(this, Handler())
  private val preferenceChangeListener =
      SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
          DND_SYNC_TO_PHONE_KEY -> {
            setDnDSyncToPhone(sharedPreferences.getBoolean(key, false))
          }
          DND_SYNC_WITH_THEATER_KEY -> {
            setDnDSyncWithTheaterMode(sharedPreferences.getBoolean(key, false))
          }
        }
      }

  override fun onBind(p0: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()

    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

    sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)

    setDnDSyncToPhone(sharedPreferences.getBoolean(DND_SYNC_TO_PHONE_KEY, false))
    setDnDSyncWithTheaterMode(sharedPreferences.getBoolean(DND_SYNC_WITH_THEATER_KEY, false))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createNotificationChannel()
    }
    startForeground(DND_SYNC_LOCAL_NOTI_ID, createNotification())
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
    NotificationCompat.Builder(this, DND_SYNC_NOTI_CHANNEL_ID)
        .apply {
          setContentTitle(getString(R.string.interrupt_filter_sync_active_noti_title))
          when {
            dndSyncToPhone and dndSyncWithTheater ->
                setContentText(getString(R.string.interrupt_filter_sync_all_noti_desc))
            dndSyncToPhone and !dndSyncWithTheater ->
                setContentText(getString(R.string.interrupt_filter_sync_to_phone_noti_desc))
            dndSyncWithTheater and !dndSyncToPhone ->
                setContentText(getString(R.string.interrupt_filter_sync_with_theater_noti_desc))
            else -> setContentText(getString(R.string.interrupt_filter_sync_none_noti_desc))
          }
          setSmallIcon(R.drawable.ic_sync)
          setOngoing(true)
          setShowWhen(false)
          setUsesChronometer(false)
          priority = NotificationCompat.PRIORITY_LOW

          val launchIntent =
              Intent(this@DnDLocalChangeListener, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
              }

          PendingIntent.getActivity(
                  this@DnDLocalChangeListener,
                  START_ACTIVITY_FROM_NOTI_ID,
                  launchIntent,
                  PendingIntent.FLAG_CANCEL_CURRENT)
              .also { setContentIntent(it) }
        }
        .also {
          return it.build()
        }
  }

  private fun setDnDSyncWithTheaterMode(enabled: Boolean) {
    if (dndSyncWithTheater != enabled) {
      dndSyncWithTheater = enabled
      notificationManager.notify(DND_SYNC_LOCAL_NOTI_ID, createNotification())
      if (enabled) {
        applicationContext.contentResolver
            .registerContentObserver(Settings.Global.CONTENT_URI, true, theaterModeObserver)
      } else {
        applicationContext.contentResolver.unregisterContentObserver(theaterModeObserver)
        tryStop()
      }
    }
  }

  private fun setDnDSyncToPhone(enabled: Boolean) {
    if (dndSyncToPhone != enabled) {
      dndSyncToPhone = enabled
      notificationManager.notify(DND_SYNC_LOCAL_NOTI_ID, createNotification())
      if (enabled) {
        IntentFilter()
            .apply { addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) }
            .also { registerReceiver(dndChangeReceiver, it) }
      } else {
        try {
          unregisterReceiver(dndChangeReceiver)
        } catch (ignored: IllegalArgumentException) {}
        tryStop()
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun createNotificationChannel() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (notificationManager.getNotificationChannel(DND_SYNC_NOTI_CHANNEL_ID) == null) {
      NotificationChannel(
              DND_SYNC_NOTI_CHANNEL_ID,
              getString(R.string.noti_channel_dnd_sync_title),
              NotificationManager.IMPORTANCE_LOW)
          .apply {
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
          }
          .also { notificationManager.createNotificationChannel(it) }
    }
  }

  private fun tryStop() {
    if (!dndSyncToPhone and !dndSyncWithTheater) {
      stopForeground(true)
      stopSelf()
    }
  }
}

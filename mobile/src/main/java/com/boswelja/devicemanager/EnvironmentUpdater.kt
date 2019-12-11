/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.WatchBatteryUpdateReceiver
import com.boswelja.devicemanager.common.interruptfiltersync.References

class EnvironmentUpdater(private val context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val currentAppVersion: Int = BuildConfig.VERSION_CODE
    private val lastAppVersion: Int
    private var notificationChannelsCreated: Boolean = false

    init {
        lastAppVersion = sharedPreferences.getInt(APP_VERSION_KEY, currentAppVersion)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelsCreated = sharedPreferences.getBoolean(NOTIFICATION_CHANNELS_CREATED, false)
        }
    }

    private fun needsUpdate(): Boolean {
        return lastAppVersion < currentAppVersion
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)!!

        if (notificationManager.getNotificationChannel(WatchBatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                    WatchBatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANNEL_ID,
                    context.getString(R.string.noti_channel_watch_charged_title),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(false)
                enableVibration(true)
                setShowBadge(true)
            }.also {
                notificationManager.createNotificationChannel(it)
            }
        }

        if (notificationManager.getNotificationChannel(References.INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                    References.INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID,
                    context.getString(R.string.noti_channel_dnd_sync_title),
                    NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }.also {
                notificationManager.createNotificationChannel(it)
            }
        }
    }

    fun doUpdate(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (!notificationChannelsCreated or needsUpdate())) {
            createNotificationChannels()
            sharedPreferences.edit().putBoolean(NOTIFICATION_CHANNELS_CREATED, true).apply()
            notificationChannelsCreated = true
        }

        if (needsUpdate()) {
            var updateComplete = false
            sharedPreferences.edit(commit = true) {
                if (lastAppVersion < 2019090243) {
                    remove("connected_watch_name")
                    updateComplete = true
                }
                if (lastAppVersion in 2019070801..2019110999) {
                    val lastConnectedId = sharedPreferences.getString("connected_watch_id", "")
                    remove("connected_watch_id")
                    putString("last_connected_id", lastConnectedId)
                    updateComplete = true
                }
                if (lastAppVersion < 2019120600) {
                    clear()
                    updateComplete = false
                }
                putInt(APP_VERSION_KEY, lastAppVersion)
            }
            return updateComplete
        }
        return false
    }

    companion object {
        private const val APP_VERSION_KEY = "app_version"
        private const val NOTIFICATION_CHANNELS_CREATED = "notification_channels_created"
    }
}

/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.R
import com.boswelja.devicemanager.common.interruptfiltersync.References
import com.boswelja.devicemanager.watchconnectionmanager.BoolPreference
import com.boswelja.devicemanager.watchconnectionmanager.IntPreference
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.boswelja.devicemanager.watchconnectionmanager.WatchPreferenceChangeInterface
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class DnDLocalChangeService :
        Service(),
        WatchPreferenceChangeInterface {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
            service.registerWatchPreferenceChangeInterface(this@DnDLocalChangeService)

            val preferences = service.getBoolPrefsForRegisteredWatches(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY)
            if (preferences != null) {
                for (preference in preferences) {
                    sendToWatch[preference.watchId] = preference.value
                }
            }

            stopIfUnneeded()
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
        }
    }

    private val dndChangeReceiver = object : DnDLocalChangeReceiver() {
        override fun onDnDChanged(dndEnabled: Boolean) {
            pushNewDnDState(this@DnDLocalChangeService, dndEnabled)
        }
    }

    private val sendToWatch = HashMap<String, Boolean>()

    private lateinit var sharedPreferences: SharedPreferences

    private var watchConnectionManager: WatchConnectionService? = null

    override fun boolPreferenceChanged(boolPreference: BoolPreference) {
        if (boolPreference.key == PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY) {
            sendToWatch[boolPreference.watchId] = boolPreference.value
            stopIfUnneeded()
        }
    }

    override fun intPreferenceChanged(intPreference: IntPreference) {}

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        WatchConnectionService.bind(this, watchConnectionManagerConnection)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        DnDLocalChangeReceiver.registerReceiver(this, dndChangeReceiver)

        pushNewDnDState(this, Utils.isDnDEnabledCompat(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(SERVICE_NOTIFICATION_ID, createNotification())

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterReceiver(dndChangeReceiver)
        } catch (ignored: IllegalArgumentException) {}

        watchConnectionManager?.unregisterWatchPreferenceChangeInterface(this)
        unbindService(watchConnectionManagerConnection)
    }

    private fun createNotification(): Notification {
        val notiTapIntent = PendingIntent.getActivity(this,
                SERVICE_NOTIFICATION_TAP_INTENT_ID,
                packageManager.getLaunchIntentForPackage(packageName),
                PendingIntent.FLAG_CANCEL_CURRENT)

        return NotificationCompat.Builder(this, References.INTERRUPT_FILTER_SYNC_NOTI_CHANNEL_ID)
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

    private fun pushNewDnDState(context: Context, dndEnabled: Boolean) {
        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create(References.DND_STATUS_PATH)
        putDataMapReq.dataMap.putBoolean(References.NEW_DND_STATE_KEY, dndEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }

    private fun shouldKeepRunning(): Boolean {
        return sendToWatch.filter { it.value }.isNotEmpty()
    }

    private fun stopIfUnneeded() {
        if (!shouldKeepRunning()) {
            stopForeground(true)
            stopSelf()
        }
    }

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 52447
        private const val SERVICE_NOTIFICATION_TAP_INTENT_ID = 30177
    }
}

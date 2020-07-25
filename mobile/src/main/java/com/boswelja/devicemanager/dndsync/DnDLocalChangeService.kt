/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.NotificationChannelHelper
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.R
import com.boswelja.devicemanager.common.dndsync.References
import com.boswelja.devicemanager.ui.main.MainActivity
import com.boswelja.devicemanager.watchmanager.WatchPreferenceChangeListener
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class DnDLocalChangeService : Service(), WatchPreferenceChangeListener {

    private val dndChangeReceiver = object : DnDLocalChangeReceiver() {
        override fun onDnDChanged(dndEnabled: Boolean) {
            pushNewDnDState(dndEnabled)
        }
    }

    private val sendToWatch = HashMap<String, Boolean>()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dataClient: DataClient

    override fun onWatchPreferenceChanged(watchId: String, preferenceKey: String, newValue: Any?) {
        if (preferenceKey == PreferenceKey.DND_SYNC_TO_WATCH_KEY) {
            val isEnabled = newValue == true
            Timber.i("$preferenceKey changed for $watchId")
            sendToWatch[watchId] = isEnabled
            if (!isEnabled) stopIfUnneeded()
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.i("onCreate() called")

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        dataClient = Wearable.getDataClient(this)

        // TOOD Regression here, service no longer stops itself
        DnDLocalChangeReceiver.registerReceiver(this, dndChangeReceiver)

        pushNewDnDState(Compat.isDndEnabled(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand() called")
        startForeground(SERVICE_NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy() called")

        try {
            unregisterReceiver(dndChangeReceiver)
        } catch (ignored: IllegalArgumentException) {}
    }

    /**
     * Create a [Notification] to show this service is in the foreground.
     * @return The created [Notification].
     */
    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannelHelper.createForDnDSync(
                this, getSystemService(NotificationManager::class.java)
            )

        val launchActivityIntent = Intent(this, MainActivity::class.java)
        val notiTapIntent = PendingIntent.getActivity(
            this,
            0, launchActivityIntent, 0
        )

        return NotificationCompat.Builder(this, References.DND_SYNC_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.interrupt_filter_sync_active_noti_title))
            .setContentText(getString(R.string.interrupt_filter_sync_to_phone_noti_desc))
            .setSmallIcon(R.drawable.ic_sync)
            .setOngoing(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(notiTapIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Push the new DnD state to the [DataClient] for any watches to retrieve.
     * @param dndEnabled The new state of Do not Disturb.
     */
    private fun pushNewDnDState(dndEnabled: Boolean) {
        val putDataMapReq = PutDataMapRequest.create(References.DND_STATUS_PATH)
        putDataMapReq.dataMap.putBoolean(References.NEW_DND_STATE_KEY, dndEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
        Timber.i("Pushed new DnD state: $dndEnabled")
    }

    /**
     * Stops the service if it doesn't need to be running any more.
     */
    private fun stopIfUnneeded() {
        Timber.i("Trying to stop service")
        if (sendToWatch.filter { it.value }.isEmpty()) {
            stopForeground(true)
            stopSelf()
        }
    }

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 52447
    }
}

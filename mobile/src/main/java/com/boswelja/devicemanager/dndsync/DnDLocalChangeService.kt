/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import com.boswelja.devicemanager.NotificationChannelHelper
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.R
import com.boswelja.devicemanager.common.dndsync.References
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.main.MainActivity
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class DnDLocalChangeService : LifecycleService() {

    private val database by lazy { WatchDatabase.get(this) }
    private val dataClient by lazy { Wearable.getDataClient(this) }

    private val dndChangeReceiver =
        object : DnDLocalChangeReceiver() {
            override fun onDnDChanged(dndEnabled: Boolean) {
                pushNewDnDState(dndEnabled)
            }
        }

    private val sendToWatch = HashMap<String, Boolean>()

    override fun onCreate() {
        super.onCreate()
        Timber.i("onCreate() called")

        dndChangeReceiver.register(this)

        pushNewDnDState(Compat.isDndEnabled(this))
        database.boolPrefDao().getAllObservableForKey(DND_SYNC_TO_WATCH_KEY).observe(this) {
            Timber.i("Watch preferences changed, updating in service")
            it.forEach { preference -> sendToWatch[preference.watchId] = preference.value }
            stopIfUnneeded()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand() called")
        startForeground(SERVICE_NOTIFICATION_ID, createNotification())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy() called")

        try {
            dndChangeReceiver.unregister(this)
        } catch (ignored: IllegalArgumentException) {}
    }

    /**
     * Create a [Notification] to show this service is in the foreground.
     * @return The created [Notification].
     */
    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannelHelper.createForDnDSync(this, getSystemService()!!)

        val launchActivityIntent = Intent(this, MainActivity::class.java)
        val notiTapIntent = PendingIntent.getActivity(
            this,
            0,
            launchActivityIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
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
        PutDataMapRequest.create(References.DND_STATUS_PATH)
            .apply {
                dataMap.putBoolean(References.NEW_DND_STATE_KEY, dndEnabled)
                setUrgent()
            }
            .also { dataClient.putDataItem(it.asPutDataRequest()) }
        Timber.i("Pushed new DnD state: $dndEnabled")
    }

    /** Stops the service if it doesn't need to be running any more. */
    private fun stopIfUnneeded() {
        if (sendToWatch.none { it.value }) {
            Timber.i("Service unneeded, stopping")
            stopForeground(true)
            stopSelf()
        }
    }

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 52447
    }
}

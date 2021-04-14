package com.boswelja.smartwatchextensions.dndsync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.asLiveData
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.Compat
import com.boswelja.smartwatchextensions.common.dndsync.References
import com.boswelja.smartwatchextensions.common.dndsync.References.DND_SYNC_LOCAL_NOTI_ID
import com.boswelja.smartwatchextensions.common.dndsync.References.DND_SYNC_NOTI_CHANNEL_ID
import com.boswelja.smartwatchextensions.common.dndsync.References.START_ACTIVITY_FROM_NOTI_ID
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.map

class DnDLocalChangeListener : LifecycleService() {

    private lateinit var notificationManager: NotificationManager

    private var dndSyncToPhone: Boolean = false
    private var dndSyncWithTheater: Boolean = false

    private var dndChangeReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (context != null &&
                    intent!!.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED
                ) {
                    updateInterruptionFilter(this@DnDLocalChangeListener)
                }
            }
        }

    private val theaterModeObserver = TheaterModeObserver(this, Handler(Looper.getMainLooper()))

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        extensionSettingsStore.data.map {
            it.dndSyncToPhone
        }.asLiveData().observe(this) {
            setDnDSyncToPhone(it)
        }
        extensionSettingsStore.data.map {
            it.dndSyncWithTheater
        }.asLiveData().observe(this) {
            setDnDSyncWithTheaterMode(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startForeground(DND_SYNC_LOCAL_NOTI_ID, createNotification())
        updateInterruptionFilter(this)
    }

    private fun createNotification(): Notification {
        NotificationCompat.Builder(this, DND_SYNC_NOTI_CHANNEL_ID)
            .apply {
                setContentTitle(getString(R.string.dnd_sync_active_noti_title))
                when {
                    dndSyncToPhone and dndSyncWithTheater ->
                        setContentText(getString(R.string.dnd_sync_all_noti_desc))
                    dndSyncToPhone and !dndSyncWithTheater ->
                        setContentText(getString(R.string.dnd_sync_to_phone_noti_desc))
                    dndSyncWithTheater and !dndSyncToPhone ->
                        setContentText(getString(R.string.dnd_sync_with_theater_noti_desc))
                    else -> setContentText(getString(R.string.dnd_sync_none_noti_desc))
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
                    PendingIntent.FLAG_IMMUTABLE
                )
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
                applicationContext.contentResolver.registerContentObserver(
                    Settings.Global.CONTENT_URI, true, theaterModeObserver
                )
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
                } catch (ignored: IllegalArgumentException) { }
                tryStop()
            }
        }
    }

    /**
     * Sets a new Interruption Filter state across devices.
     */
    private fun updateInterruptionFilter(context: Context) {
        val interruptionFilterEnabled = Compat.isDndEnabled(context)
        val dataClient = Wearable.getDataClient(context)
        val putDataMapReq = PutDataMapRequest.create(References.DND_STATUS_PATH)
        putDataMapReq.dataMap.putBoolean(References.NEW_DND_STATE_KEY, interruptionFilterEnabled)
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.getNotificationChannel(DND_SYNC_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                DND_SYNC_NOTI_CHANNEL_ID,
                getString(R.string.noti_channel_dnd_sync_title),
                NotificationManager.IMPORTANCE_LOW
            )
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

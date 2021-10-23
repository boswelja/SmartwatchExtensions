package com.boswelja.smartwatchextensions.dndsync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * A [LifecycleService] that listens for changes in the appropriate settings, and sends DnD change
 * requests to the connected phone.
 */
class LocalDnDAndTheaterCollectorService : BaseLocalDnDAndTheaterCollectorService() {

    private val discoveryClient: DiscoveryClient by inject()
    private val messageClient: MessageClient by inject()

    private var dndSyncToPhone: Boolean = false
    private var dndSyncWithTheater: Boolean = false

    override suspend fun onTheaterChanged(theaterState: Boolean) {
        if (dndSyncWithTheater) {
            updateDnDState(theaterState)
        }
    }

    override suspend fun onDnDChanged(dndState: Boolean) {
        if (dndSyncToPhone) {
            updateDnDState(dndState)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(DND_SYNC_LOCAL_NOTI_ID, createNotification())

        // Collect DnD Sync to Phone
        lifecycleScope.launch {
            extensionSettingsStore.data.map {
                Pair(it.dndSyncToPhone, it.dndSyncWithTheater)
            }.collect { (dndSync, theaterSync) ->
                dndSyncToPhone = dndSync
                dndSyncWithTheater = theaterSync
                if (!tryStop()) {
                    startForeground(DND_SYNC_LOCAL_NOTI_ID, createNotification())
                }
            }
        }
    }

    /**
     * Create a foreground notification for this service.
     * @return The notification to post for this service.
     */
    private fun createNotification(): Notification {
        NotificationCompat.Builder(this, DND_SYNC_NOTI_CHANNEL_ID)
            .apply {
                setContentTitle(getString(R.string.dnd_sync_active_noti_title))
                when {
                    dndSyncToPhone && dndSyncWithTheater ->
                        setContentText(getString(R.string.dnd_sync_all_noti_desc))
                    dndSyncToPhone && !dndSyncWithTheater ->
                        setContentText(getString(R.string.dnd_sync_to_phone_noti_desc))
                    dndSyncWithTheater && !dndSyncToPhone ->
                        setContentText(getString(R.string.dnd_sync_with_theater_noti_desc))
                    else ->
                        setContentText(getString(R.string.getting_ready))
                }
                setSmallIcon(R.drawable.ic_sync)
                setOngoing(true)
                setShowWhen(false)
                setUsesChronometer(false)
                priority = NotificationCompat.PRIORITY_LOW

                val launchIntent =
                    Intent(this@LocalDnDAndTheaterCollectorService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                PendingIntent.getActivity(
                    this@LocalDnDAndTheaterCollectorService,
                    START_ACTIVITY_FROM_NOTI_ID,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE
                ).also { setContentIntent(it) }
            }
            .also {
                return it.build()
            }
    }

    /**
     * Sets a new DnD state across devices.
     * @param dndSyncEnabled Whether Interruption Filter should be enabled.
     */
    private suspend fun updateDnDState(dndSyncEnabled: Boolean) {
        messageClient.sendMessage(
            discoveryClient.pairedPhone()!!,
            Message(DND_STATUS_PATH, dndSyncEnabled)
        )
    }

    /**
     * Creates a [NotificationChannel] for [DND_SYNC_NOTI_CHANNEL_ID].
     */
    fun createNotificationChannel() {
        val notificationManager = getSystemService<NotificationManager>()!!
        if (notificationManager.getNotificationChannel(DND_SYNC_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                DND_SYNC_NOTI_CHANNEL_ID,
                getString(R.string.noti_channel_dnd_sync_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }.also { notificationManager.createNotificationChannel(it) }
        }
    }

    /**
     * Stop the service if all sync features are disabled.
     * @return true if we stopped the service, false otherwise.
     */
    private fun tryStop(): Boolean {
        if (!dndSyncToPhone && !dndSyncWithTheater) {
            stopForeground(true)
            stopSelf()
            return true
        }
        return false
    }
}

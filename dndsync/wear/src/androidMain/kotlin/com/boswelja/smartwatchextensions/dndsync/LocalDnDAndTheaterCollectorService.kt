package com.boswelja.smartwatchextensions.dndsync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.core.devicemanagement.phoneStateStore
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.serialization.MessageHandler
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * A [LifecycleService] that listens for changes in the appropriate settings, and sends DnD change
 * requests to the connected phone.
 */
class LocalDnDAndTheaterCollectorService : BaseLocalDnDCollectorService() {

    private val phoneState by lazy { phoneStateStore }
    private val messageClient: MessageClient by inject()
    private val dnDSyncStateRepository: DnDSyncStateRepository by inject()
    private val messageHandler: MessageHandler<Boolean> by lazy {
        MessageHandler(DnDStatusSerializer, messageClient)
    }

    private var dndSyncToPhone: Boolean = false
    private var dndSyncWithTheater: Boolean = false

    override suspend fun onDnDChanged(dndState: Boolean) {
        if (dndSyncToPhone) {
            updateDnDState(dndState)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(DnDSyncLocalNotiId, createNotification())

        // Collect DnD Sync to Phone
        lifecycleScope.launch {
            dnDSyncStateRepository.getDnDSyncState().collect {
                dndSyncToPhone = it.dndSyncToPhone
                dndSyncWithTheater = it.dndSyncWithTheater
                if (!tryStop()) {
                    startForeground(DnDSyncLocalNotiId, createNotification())
                }
            }
        }

        // Collect theater mode changes
        lifecycleScope.launch {
            contentResolver.theaterMode().collect { theaterMode ->
                onTheaterChanged(theaterMode)
            }
        }
    }

    private suspend fun onTheaterChanged(theaterState: Boolean) {
        if (dndSyncWithTheater) {
            updateDnDState(theaterState)
        }
    }

    /**
     * Create a foreground notification for this service.
     * @return The notification to post for this service.
     */
    private fun createNotification(): Notification {
        NotificationCompat.Builder(this, DnDSyncNotiChannelId)
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
                        setContentText(getString(R.string.dnd_sync_starting))
                }
                setSmallIcon(com.boswelja.smartwatchextensions.dndsync.common.R.drawable.ic_sync)
                setOngoing(true)
                setShowWhen(false)
                setUsesChronometer(false)
                priority = NotificationCompat.PRIORITY_LOW

                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

                PendingIntent.getActivity(
                    this@LocalDnDAndTheaterCollectorService,
                    StartActivityIntentId,
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
        val phoneId = phoneState.data.map { it.id }.first()
        messageHandler.sendMessage(
            phoneId,
            Message(DnDStatusPath, dndSyncEnabled)
        )
    }

    /**
     * Creates a [NotificationChannel] for [DnDSyncNotiChannelId].
     */
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager.getNotificationChannel(DnDSyncNotiChannelId) == null) {
            NotificationChannel(
                DnDSyncNotiChannelId,
                getString(com.boswelja.smartwatchextensions.dndsync.common.R.string.noti_channel_dnd_sync_title),
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

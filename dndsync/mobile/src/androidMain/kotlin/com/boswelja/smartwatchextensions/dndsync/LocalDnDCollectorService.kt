package com.boswelja.smartwatchextensions.dndsync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_TO_WATCH_KEY
import com.boswelja.smartwatchextensions.dndsync.common.R
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * An implementation of [BaseLocalDnDCollectorService] that observes the setting state for all
 * registered watches.
 */
class LocalDnDCollectorService : BaseLocalDnDCollectorService() {

    private val settingsRepository: WatchSettingsRepository by inject()
    private val messageClient: MessageClient by inject()
    private val messageHandler by lazy { MessageHandler(DnDStatusSerializer, messageClient) }

    private val targetWatches = ArrayList<String>()

    override suspend fun onDnDChanged(dndState: Boolean) {
        sendNewDnDState(dndState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        lifecycleScope.launch {
            settingsRepository
                .getIdsWithBooleanSet(DND_SYNC_TO_WATCH_KEY, true)
                .collect {
                    targetWatches.clear()
                    targetWatches.addAll(it)
                    stopIfUnneeded()
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(SERVICE_NOTIFICATION_ID, createNotification())
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Create a [Notification] to show this service is in the foreground.
     * @return The created [Notification].
     */
    private fun createNotification(): Notification {
        createNotificationChannel(getSystemService(NotificationManager::class.java))
        val launchActivityIntent = packageManager.getLaunchIntentForPackage(packageName)
        val notiTapIntent = PendingIntent.getActivity(
            this,
            0,
            launchActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DND_SYNC_NOTI_CHANNEL_ID)
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
     * Notify target watches of the DnD state change.
     * @param dndEnabled The new state of Do not Disturb.
     */
    private suspend fun sendNewDnDState(dndEnabled: Boolean) {
        targetWatches.forEach { watchUid ->
            messageHandler.sendMessage(
                watchUid,
                Message(
                    DND_STATUS_PATH,
                    dndEnabled
                )
            )
        }
    }

    /** Stops the service if it doesn't need to be running any more. */
    private fun stopIfUnneeded() {
        if (targetWatches.isEmpty()) {
            stopForeground(true)
            stopSelf()
        }
    }

    /**
     * Create a notification channel for DnD Sync status notifications.
     */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(DND_SYNC_NOTI_CHANNEL_ID) ==
            null
        ) {
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

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 52447
    }
}
